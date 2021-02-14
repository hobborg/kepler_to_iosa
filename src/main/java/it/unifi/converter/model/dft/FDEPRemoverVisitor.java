//==============================================================================
//
//  FDEPRemoverVisitor.java
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


package it.unifi.converter.model.dft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unifi.converter.model.dft.event.BasicElement;
import it.unifi.converter.model.dft.ports.AndPort;
import it.unifi.converter.model.dft.ports.FailDependencyPort;
import it.unifi.converter.model.dft.ports.FunctionalDependencyPort;
import it.unifi.converter.model.dft.ports.KOfNPort;
import it.unifi.converter.model.dft.ports.OrPort;
import it.unifi.converter.model.dft.ports.PriorityAndPort;
import it.unifi.converter.model.dft.ports.SparePort;
import it.unifi.converter.model.dft.ports.repairbox.RepairBox;

/**
 * This class allows, given a DFT, to build an equivalent tree without FDEP gates
 * 
 * Each BE affected by FDEP gate is replaced with an OR port with the trigger and the original BE as input
 * 
 * @author Marco Biagi
 *
 */
public class FDEPRemoverVisitor implements TreeNodeVisitor{

    private DFT originalDFT;
    private DFT equivalentDFT;
    Map<TreeNode, TreeNode> convertedNodes;
    Map<TreeNode, TreeNode> originalBEs;//Keep track of the original BE in order to maintain the reference in the RBOXes
    List<RepairBox> convertedRepairBoxes;
    List<FailDependencyPort> convertedFXDEP;

    public FDEPRemoverVisitor(DFT dft){
        originalDFT = dft;
        convertedNodes = new HashMap<>(); 
        originalBEs = new HashMap<>();
        convertedRepairBoxes = new ArrayList<>();
        convertedFXDEP = new ArrayList<>();
        
        dft.getRoot().accept(this);
        dft.getFailDependencyPorts().forEach(e -> e.accept(this));
        dft.getRepairBoxes().forEach(e -> e.accept(this));
        
        this.equivalentDFT = new DFT(convertedNodes.get(dft.getRoot()), convertedRepairBoxes, new ArrayList<>(), convertedFXDEP);
    }
    
    public DFT getEquivalentTree() {
        return equivalentDFT;
    }
    
    @Override
    public void visit(BasicElement n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.containsKey(n))
            return;
        //Get dependencies
        List<FunctionalDependencyPort> deps = getListFunctionalDependancy(n);
        List<FailDependencyPort> deps2 = getListFailDependancy(n);
        //If no return it as is
        if(deps.isEmpty()){
            BasicElement newBE = new BasicElement(n.getName(), n.getDormancyFactor(), n.getFailureDistribution(), n.getRepairDistribution(), n.getDormancyDistribution());
            convertedNodes.put(n, newBE);
            originalBEs.put(n, newBE);
            return;
        }
        //If yes, create an OR port and link the port with
        OrPort fdepOrPort = new OrPort(n.getName() + "_FDEP");
        List<TreeNode> inputs = new ArrayList<>();
        //Add trigger
        for (FunctionalDependencyPort dep : deps) {
            if(!convertedNodes.containsKey(dep.getTriggerInput()))//If not yet visited
                dep.getTriggerInput().accept(this);
            inputs.add(convertedNodes.get(dep.getTriggerInput()));
        }
        //Add BE
        BasicElement newBE = new BasicElement(n.getName(), n.getDormancyFactor(), n.getFailureDistribution(), n.getRepairDistribution(), n.getDormancyDistribution());
        inputs.add(newBE);
        
        fdepOrPort.setInputs(inputs);
        
        //Replace be with the OR port
        convertedNodes.put(n, fdepOrPort);
        originalBEs.put(n, newBE);
        
        //TODO handle following case
        if(!deps2.isEmpty())
            throw new UnsupportedOperationException("BE " + n.getName() + " has both FDEP and FXDEP. We are not sure that this case is corrrectly handled...");        
    }
    
    private List<FunctionalDependencyPort> getListFunctionalDependancy(BasicElement n){
        List<FunctionalDependencyPort> deps = new ArrayList<>();
        for (FunctionalDependencyPort fdepPort : originalDFT.getFunctionalDependencyPorts()) {
            if(fdepPort.getAffectedNodes().contains(n))
                deps.add(fdepPort);
        }
        return deps;
    }

    private List<FailDependencyPort> getListFailDependancy(BasicElement n){
        List<FailDependencyPort> deps = new ArrayList<>();
        for (FailDependencyPort fdepPort : originalDFT.getFailDependencyPorts()) {
            if(fdepPort.getAffectedNodes().contains(n))
                deps.add(fdepPort);
        }
        return deps;
    }

    
    @Override
    public void visit(AndPort n) {
       //If dependent node are present, this node is already been converted!
       if(convertedNodes.containsKey(n))
            return;
       //Visit children
       List<TreeNode> newChildren = new ArrayList<>();
       for(TreeNode child: n.getInputs()){
           child.accept(this);
           newChildren.add(convertedNodes.get(child));
       }
       //Create a copy of the original port
       AndPort newAndPort = new AndPort(n.getName());
       newAndPort.setInputs(newChildren);
       convertedNodes.put(n, newAndPort);
    }

    @Override
    public void visit(FunctionalDependencyPort n) {
        //DO NOTHING        
    }

    @Override
    public void visit(FailDependencyPort n) {
        //Find children
        n.getTriggerInput().accept(this);
        TreeNode newTrigger = convertedNodes.get(n.getTriggerInput());
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getAffectedNodes()){
            newChildren.add(convertedNodes.get(child));
        }
        
        newChildren.add(0, newTrigger);
        
        //Create a copy of the original port
        FailDependencyPort newPort = new FailDependencyPort(n.getName());
        newPort.setInputs(newChildren);
        convertedFXDEP.add(newPort);
    }

    @Override
    public void visit(KOfNPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.containsKey(n))
            return;
        //Visit children
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getInputs()){
            child.accept(this);
            newChildren.add(convertedNodes.get(child));
        }
        //Create a copy of the original port
        KOfNPort newKOfNPort = new KOfNPort(n.getName(), n.getK(), n.getN());
        newKOfNPort.setInputs(newChildren);
        convertedNodes.put(n, newKOfNPort);
    }

    @Override
    public void visit(OrPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.containsKey(n))
            return;
        //Visit children
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getInputs()){
            child.accept(this);
            newChildren.add(convertedNodes.get(child));
        }
        //Create a copy of the original port
        OrPort newOrPort = new OrPort(n.getName());
        newOrPort.setInputs(newChildren);
        convertedNodes.put(n, newOrPort);
        
    }

    @Override
    public void visit(PriorityAndPort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.containsKey(n))
            return;
        //Visit children
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getInputs()){
            child.accept(this);
            newChildren.add(convertedNodes.get(child));
        }
        //Create a copy of the original port
        PriorityAndPort newPAndPort = new PriorityAndPort(n.getName());
        newPAndPort.setInputs(newChildren);
        convertedNodes.put(n, newPAndPort);
    }

    @Override
    public void visit(SparePort n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.containsKey(n))
            return;
        //Visit children
        n.getPrimaryInput().accept(this);
        TreeNode newPrimary = convertedNodes.get(n.getPrimaryInput());
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getSpareInputs()){
            child.accept(this);
            newChildren.add(convertedNodes.get(child));
        }
        
        newChildren.add(0, newPrimary);
        
        //Create a copy of the original port
        SparePort newPort = new SparePort(n.getName(), n.getType());
        newPort.setInputs(newChildren);
        convertedNodes.put(n, newPort);
    }

    @Override
    public void visit(RepairBox n) {
        //Find children
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getManagedNodes()){
            newChildren.add(originalBEs.get(child));
        }
        
        //Create a copy of the original port
        RepairBox newPort = new RepairBox(n.getName(), n.getPolicy());
        newPort.setInputs(newChildren);
        convertedRepairBoxes.add(newPort);
    }

}
