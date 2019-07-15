//==============================================================================
//
//  RepairBox.java
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


package it.unifi.converter.model.dft.ports.repairbox;

import java.util.ArrayList;
import java.util.List;

import it.unifi.converter.model.dft.FormatVisitor;
import it.unifi.converter.model.dft.TreeNode;
import it.unifi.converter.model.dft.TreeNodeVisitor;
import it.unifi.converter.model.dft.ports.TreePort;

public class RepairBox extends TreePort {

    public enum Policy{
        random,
        priority,
        fcfs,
        single
    }

    private Policy policy;
    private List<TreeNode> managedNodes;;
    
    public RepairBox(String name, Policy policy) {
        super(name);
        this.policy = policy;
    }

    @Override
    public void accept(TreeNodeVisitor v) {
        v.visit(this);
    }

    public Policy getPolicy() {
        return policy;
    }

    @Override
    public void setInputs(List<TreeNode> inputs) {
        if(inputs == null || inputs.size() == 0)
            throw new IllegalArgumentException("Repair box " + this.getName() + " should be associated to at list one node");
        managedNodes = inputs;
    }

    public List<TreeNode> getManagedNodes() {
        return managedNodes;
    }

    @Override
    public List<TreeNode> getInputs() {
        return new ArrayList<>(managedNodes);
    }
    
    @Override
    public String format(FormatVisitor v) {
        return v.visit(this);
    }

 
}
