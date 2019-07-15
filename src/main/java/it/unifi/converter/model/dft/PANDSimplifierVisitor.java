//==============================================================================
//
//  PANDSimplifierVisitor.java
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

import it.unifi.converter.model.dft.event.BasicEvent;
import it.unifi.converter.model.dft.ports.AndPort;
import it.unifi.converter.model.dft.ports.FailDependencyPort;
import it.unifi.converter.model.dft.ports.FunctionalDependencyPort;
import it.unifi.converter.model.dft.ports.KOfNPort;
import it.unifi.converter.model.dft.ports.OrPort;
import it.unifi.converter.model.dft.ports.PriorityAndPort;
import it.unifi.converter.model.dft.ports.SparePort;
import it.unifi.converter.model.dft.ports.repairbox.RepairBox;

/**
 * This class allows, given a DFT, to replace n-inputs PAND with cascade of 2-inputs PAND
 * 
 * @author Marco Biagi
 *
 */
public class PANDSimplifierVisitor implements TreeNodeVisitor{

    private DFT equivalentDFT;
    Map<TreeNode, TreeNode> convertedNodes;
    List<RepairBox> convertedRepairBoxes;
    List<FailDependencyPort> convertedFXDEP;
    List<FunctionalDependencyPort> convertedFDEP;

    public PANDSimplifierVisitor(DFT dft){
        convertedNodes = new HashMap<>(); 
        convertedRepairBoxes = new ArrayList<>();
        convertedFXDEP = new ArrayList<>();
        convertedFDEP = new ArrayList<>();
        
        dft.getRoot().accept(this);
        dft.getFailDependencyPorts().forEach(e -> e.accept(this));
        dft.getRepairBoxes().forEach(e -> e.accept(this));
        dft.getFunctionalDependencyPorts().forEach(e -> e.accept(this));
        
        this.equivalentDFT = new DFT(convertedNodes.get(dft.getRoot()), convertedRepairBoxes, convertedFDEP, convertedFXDEP);
    }
    
    public DFT getEquivalentTree() {
        return equivalentDFT;
    }
    
    @Override
    public void visit(BasicEvent n) {
        //If dependent node are present, this node is already been converted!
        if(convertedNodes.containsKey(n))
            return;
        BasicEvent newBE = new BasicEvent(n.getName(), n.getDormancyFactor(), n.getFailureDistribution(), n.getRepairDistribution(), n.getDormancyDistribution());
        convertedNodes.put(n, newBE);
                
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
        //Find children
        n.getTriggerInput().accept(this);
        TreeNode newTrigger = convertedNodes.get(n.getTriggerInput());
        List<TreeNode> newChildren = new ArrayList<>();
        for(TreeNode child: n.getAffectedNodes()){
            newChildren.add(convertedNodes.get(child));
        }
        
        newChildren.add(0, newTrigger);
        
        //Create a copy of the original port
        FunctionalDependencyPort newPort = new FunctionalDependencyPort(n.getName());
        newPort.setInputs(newChildren);
        convertedFDEP.add(newPort);
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
        if(n.getInputs().size() < 2){
            throw new IllegalStateException("The priority AND " + n.getName() + " should has at least 2 inputs!");
        }
        if(n.getInputs().size() == 2){
            //Create only a simple copy since its already a 2-inputs PAND
            PriorityAndPort newPort = new PriorityAndPort(n.getName());
            newPort.setInputs(newChildren);
            convertedNodes.put(n, newPort);
        }
        //The PAND port has n-inputs, create n-1 PAND ports with 2 inputs each
        //The top level PAND will be the one that parent will use as input
        PriorityAndPort lastCreated = null; 
        for (int i = 1; i < n.getInputs().size(); i++) {// n-1 steps
            PriorityAndPort newPort = new PriorityAndPort(n.getName() + "_level_" + i);
            List<TreeNode> twoChildren = new ArrayList<>();
            if(i == 1){
                twoChildren.add(newChildren.get(0));
                twoChildren.add(newChildren.get(1));
            }else{
                twoChildren.add(lastCreated);
                twoChildren.add(newChildren.get(i));
            }
            newPort.setInputs(twoChildren);
            lastCreated = newPort;
        }
        convertedNodes.put(n, lastCreated);
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
            newChildren.add(convertedNodes.get(child));
        }
        
        //Create a copy of the original port
        RepairBox newPort = new RepairBox(n.getName(), n.getPolicy());
        newPort.setInputs(newChildren);
        convertedRepairBoxes.add(newPort);
    }

}
