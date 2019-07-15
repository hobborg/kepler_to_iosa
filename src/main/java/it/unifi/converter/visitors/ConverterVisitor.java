//==============================================================================
//
//  ConverterVisitor.java
//
//  Copyright 2018-
//  Authors:
//  - Marco Biagi <marcobiagiing@gmail.com> (Universita di Firenze)
//
//------------------------------------------------------------------------------
//
//  This file is part of the Galileo-to-IOSA converter.
//
//  The Galileo-to-IOSA converter is free software;
//  you can redistribute it and/or modify it under the terms of the GNU
//  General Public License as published by the Free Software Foundation;
//  either version 3 of the License, or (at your option) any later version.
//
//  The Galileo-to-IOSA converter is distributed in the hope that it will be
//	useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with this file; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//==============================================================================


package it.unifi.converter.visitors;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import it.unifi.converter.model.dft.DFT;
import it.unifi.converter.model.dft.TreeNode;
import it.unifi.converter.model.dft.TreeNodeVisitor;
import it.unifi.converter.model.dft.event.BasicEvent;
import it.unifi.converter.model.dft.ports.AndPort;
import it.unifi.converter.model.dft.ports.FailDependencyPort;
import it.unifi.converter.model.dft.ports.FunctionalDependencyPort;
import it.unifi.converter.model.dft.ports.KOfNPort;
import it.unifi.converter.model.dft.ports.OrPort;
import it.unifi.converter.model.dft.ports.PriorityAndPort;
import it.unifi.converter.model.dft.ports.SparePort;
import it.unifi.converter.model.dft.ports.SparePort.SpareType;
import it.unifi.converter.model.dft.ports.TreePort;
import it.unifi.converter.model.dft.ports.repairbox.RepairBox;

/**
 * The IOSA model creation is implemented with a depth first strategy, in this way inputs are created first.
 * 
 * TODO: currently SPARE input affected by FDEP are not supported. Is not easy to add support to this scenario in this case. In the following we
 * give a small overview on how to handle this case:
 * - the spare module expects to have a mux linked to each of its spare input
 * - for each spare inputs basic event there is a mux
 * - each event has a single MUX and each MUX can be associated to more than one spare port
 * - its spare input is an OR gate, so we need to add the mux on top of the OR gate
 * - spare gate need to know: 
 *      + the ID of the OR gate --> because if the BE fail, spare gate need to be informed
 *      + the ID of the MUX --> because requesting a spare require communication with MUX
 * - mux need to know: 
 *      + the ID of the spare gate --> because assigning a spare require communication with SPARE gate
 *      + the ID of the OR gate--> if the BE fail due to the FDEP trigger or due to the BE, need to be informed
 *      + the ID of the BE--> if the spare is assigned or released, we need to inform the BE
 * - be is passive and doesn't need to know any ID of other components
 * 
 * @author Marco Biagi
 *
 */
public class ConverterVisitor implements TreeNodeVisitor{

    private static final String BASE_TEMPLATE_FOLDERS = "templates/";
    private Writer writer;
    private VelocityEngine ve;
    private long counterId;
    private Map<TreeNode, Long> assignedNodeId;
    private Map<BasicEvent, Long> assignedMuxId;//For each spare inputs basic event there is a mux. This map contain the id
    private Set<TreeNode> convertedNodes; //We need to have a distinct set to keep track of already built node: assignedId is not sufficient since in some cases id is created before
    private DFT dft;
    
    public ConverterVisitor(Writer writer, DFT dft){
        this.writer = writer;
        this.dft = dft;
        this.ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        this.counterId = 0;
        this.assignedNodeId = new HashMap<>();
        this.assignedMuxId = new HashMap<>();
        this.convertedNodes = new HashSet<>();
        if(dft.getFunctionalDependencyPorts() != null && dft.getFunctionalDependencyPorts().size() > 0)
            throw new InternalError("FDEP gate found in the DFT. Before converting to IOSA-C you need to use FDEPConverter to convert them in OR gates!");
        
    }
    
    /**
     * This method return a map of the ids assigned to each TreeNode visited
     * 
     * @return map of assigned id
     */
    public Map<TreeNode, Long> getAssignedId(){
        return Collections.unmodifiableMap(assignedNodeId);
    }
    
    @Override
    public void visit(BasicEvent n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.contains(n))
            return;
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //Check type of basic event
        List<Long> triggerFailIds = getListTriggerIdsAndCreateTriggerIfRequired(n);
        
        List<Long> associatedSparePortIds =  getListAssociatedSparePorts(n);
        boolean isInputToSpare = !associatedSparePortIds.isEmpty();
        
        //Define basic context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("failureDistribution", n.getFailureDistribution().asFIGFormatString());
        if(n.getRepairDistribution() != null)
            context.put("repairDistribution", n.getRepairDistribution().asFIGFormatString());
        if(n.getDormancyDistribution() != null)
            context.put("dormancyDistribution", n.getDormancyDistribution().asFIGFormatString());
        context.put("triggerFailIds", triggerFailIds);

        //Get the template and add context variables if necessary
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/be/BasicEventTemplate.vm" );
        
        if(isInputToSpare){
            //Each event has a single MUX and each MUX can be associated to more than one spare port
            long beId = assignIdIfRequired(n);
            context.put("id", beId);
            //Build the MUX (we need only to know the list of spare ports associated and the event id)
            buildMux(n, associatedSparePortIds);
        }
                
        convertedNodes.add(n);
        t.merge( context, writer );
    }

    private void buildMux(BasicEvent n, List<Long> associatedSparePortIds) {
        //1- build the MUX
        long muxId = assignMuxIdIfRequired(n);
        long beId = assignedNodeId.get(n);
        VelocityContext context = new VelocityContext();
        context.put("name", "MUX_for_" + n.getName());
        context.put("id", muxId);
        context.put("basicEventId", beId);
        context.put("associatedSparePortIds", associatedSparePortIds);
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/multiplexer/MultiplexerTemplate.vm");
        t.merge( context, writer );
    }

    private List<Long> getListAssociatedSparePorts(BasicEvent n) {
        Set<Long> associatedSparePortsIds = new HashSet<>();//Guarantee uniqueness
        //Search for spare port in the tree and check if specified basic event is an input
        Queue<TreeNode> toBeVisited = new LinkedList<>();
        toBeVisited.add(dft.getRoot());
        while(!toBeVisited.isEmpty()){
            TreeNode current = toBeVisited.poll();
            if(current instanceof SparePort){
                SparePort port = (SparePort)current;
                Long spareId = isSpareInput(port, n);
                if(spareId != null)
                    associatedSparePortsIds.add(spareId);
            }else if(current instanceof TreePort){
                TreePort port = (TreePort)current;
                toBeVisited.addAll(port.getInputs());
            }
        }
        return new ArrayList<>(associatedSparePortsIds);
    }
    
    /**
     * Check if the basic event is input as spare of the port.
     * This includes two cases:
     * - it is directly a spare input
     * - it is a spare input, but it is also controlled by a FDEP, in this case an OR gate was previously added to represent the relation
     * NOTE1: In the latter case, we check only if the second input to the spare gate is the specified input since the first one is the FDEP trigger.
     * This means that order is important in this case!
     * NOTE2: We don't need to handle the case of a gate as spare input not due to a FDEP since they are not supported at all by the current implementation.
     * @see  it.unifi.converter.model.dft.GalileoFiles#checkStructure(TreeNode)
     * 
     * @return null if not a spare input, otherwise return the id
     */
    private Long isSpareInput(SparePort spareGate, BasicEvent be){
        //Directly a spare input
        if(spareGate.getSpareInputs().contains(be)){
            long spareId = assignIdIfRequired(spareGate);
            return spareId;
        }
        for(TreeNode node: spareGate.getSpareInputs()){
            if(node instanceof TreePort){
                TreePort gate = (TreePort) node;
                if(!(gate instanceof OrPort) || gate.getInputs().size() != 2)
                    throw new InternalError("Something went wrong! Expecting an OR gate with two inputs as input to the SPARE gate " + spareGate.getName());
                if(gate.getInputs().get(1).equals(be)){
                    throw new UnsupportedOperationException("FDEP applied to spare gate input is not currently supported!");
                }
            }
                
        }
        
        return null;
    }

    private List<Long> getListTriggerIdsAndCreateTriggerIfRequired(TreeNode n) {
        List<Long> triggerFailIds = new ArrayList<>();
        for (FailDependencyPort fxdepPort : dft.getFailDependencyPorts()) {
            if(fxdepPort.getAffectedNodes().contains(n)){
                TreeNode trigger = fxdepPort.getTriggerInput();
                trigger.accept(this);//Create the node if not yet created
                long triggerId = assignedNodeId.get(trigger);
                triggerFailIds.add(triggerId);
            }
        }
        return triggerFailIds;
    }

    @Override
    public void visit(AndPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.contains(n))
            return;
        //1- Depth first
        Set<Long> childrenAssignedId = new LinkedHashSet<>();
        for (TreeNode child : n.getInputs()) {
            child.accept(this);
            childrenAssignedId.add(assignedNodeId.get(child));
        }
        
        //2-Create module for and port
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //Get the template
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/gate/AndGateTemplate.vm" );
        
        //Define the context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("childrenIdList", childrenAssignedId);

        //Generate the module
        convertedNodes.add(n);
        t.merge( context, writer );
    }

    @Override
    public void visit(FunctionalDependencyPort n) {
        throw new InternalError("FDEP gate found in the DFT. Before converting to IOSA-C you need to use FDEPRemoverVisitor to convert them in OR gates!");
    }
    
    @Override
    public void visit(FailDependencyPort n) {
       //DO NOTHING!
       //Trigger input is configured directly in the BE
    }

    @Override
    public void visit(KOfNPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.contains(n))
            return;
        //1- Depth first
        Set<Long> childrenAssignedId = new LinkedHashSet<>();
        for (TreeNode child : n.getInputs()) {
            child.accept(this);
            childrenAssignedId.add(assignedNodeId.get(child));
        }
        
        //2-Create module for and port
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //Get the template
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/gate/VotingGateTemplate.vm" );
        
        //Define the context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("childrenIdList", childrenAssignedId);
        context.put("threshold", n.getK());

        //Generate the module
        convertedNodes.add(n);
        t.merge( context, writer );
    }

    @Override
    public void visit(OrPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.contains(n))
            return;
        //1- Depth first
        Set<Long> childrenAssignedId = new LinkedHashSet<>();
        for (TreeNode child : n.getInputs()) {
            child.accept(this);
            childrenAssignedId.add(assignedNodeId.get(child));
        }
        
        //2-Create module for and port
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //Get the template
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/gate/OrGateTemplate.vm" );
        
        //Define the context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("childrenIdList", childrenAssignedId);

        //Generate the module
        convertedNodes.add(n);
        t.merge( context, writer );
    }

    @Override
    public void visit(PriorityAndPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.contains(n))
            return;
        if(n.getInputs().size() > 2)
            throw new InternalError("DFT has a priority AND with more that 2-inputs: it is not handled by ConvnverterVisitor, you need first use PANDSimplifierVisitor");
        if(n.getInputs().size() < 2)
            throw new IllegalArgumentException("PAND need to have at least 2 inputs");
        //1- Depth first
        List<Long> childrenAssignedId = new ArrayList<>();
        for (TreeNode child : n.getInputs()) {
            child.accept(this);
            childrenAssignedId.add(assignedNodeId.get(child));
        }
        
        //2-Create module for and port
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //Get the template
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/gate/PriorityAndGateTemplate.vm" );
        
        //Define the context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("firstChildId", childrenAssignedId.get(0));
        context.put("secondChildId", childrenAssignedId.get(1));

        //Generate the module
        convertedNodes.add(n);
        t.merge( context, writer );
    }

    @Override
    public void visit(SparePort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.contains(n))
            return;
        if(n.getType().equals(SpareType.cold))
            throw new UnsupportedOperationException("Cold spare gate is not yet implemented");
        
        //1- Depth first
        n.getPrimaryInput().accept(this);
        Long primaryId = assignedNodeId.get(n.getPrimaryInput());
        
        Set<Long> muxIdListOfCurrentGate = new LinkedHashSet<>();
        Set<Long> basicEventIdList = new LinkedHashSet<>();
        for (TreeNode child : n.getSpareInputs()) {
            child.accept(this);
            muxIdListOfCurrentGate.add(assignedMuxId.get(child));
            basicEventIdList.add(assignedNodeId.get(child));
        }
        
        //2-Create module for the port
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //Get the template
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/gate/SpareGateTemplate.vm" );
        
        //Define the context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("primaryId", primaryId);
        context.put("basicEventIdList", basicEventIdList);
        context.put("muxIdList", muxIdListOfCurrentGate);

        //Generate the module
        convertedNodes.add(n);
        t.merge( context, writer );
    }

    @Override
    public void visit(RepairBox n) {
        //Assign an id
        long id = assignIdIfRequired(n);
        
        //When visiting RepairBox, we assume that all managed nodes has already an assigned id
        Set<Long> childrenAssignedId = new LinkedHashSet<>();
        for (TreeNode child : n.getManagedNodes()) {
            childrenAssignedId.add(assignedNodeId.get(child));
        }
        
        //Get the template based on the type
        Template t;
        if(n.getPolicy().equals(RepairBox.Policy.priority))
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/rbox/RepairBoxPriorityTemplate.vm" );
        else if(n.getPolicy().equals(RepairBox.Policy.random))
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/rbox/RepairBoxRandomTemplate.vm" );
        else if(n.getPolicy().equals(RepairBox.Policy.single))
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/rbox/RepairBoxSingleTemplate.vm" );
        else
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/rbox/RepairBoxFCFSTemplate.vm" );
        
        //Define the context variables
        VelocityContext context = new VelocityContext();
        context.put("name", n.getName());
        context.put("id", id);
        context.put("childrenIdList", new ArrayList<>(childrenAssignedId));

        //Generate the module
        t.merge( context, writer );
    }
    
    /**
     * If a node does not have an id yet, we assign a new id to it and we return it, 
     * otherwise we return the already assigned id
     */
    private long assignIdIfRequired(TreeNode n){
        if(assignedNodeId.get(n) != null)
            return assignedNodeId.get(n);
        
        long id = counterId++;
        assignedNodeId.put(n, id);
        return id;
    }
    
    private long assignMuxIdIfRequired(BasicEvent n){
        if(assignedMuxId.get(n) != null)
            return assignedMuxId.get(n);
        
        long id = counterId++;
        assignedMuxId.put(n, id);
        return id;
    }

    

}
