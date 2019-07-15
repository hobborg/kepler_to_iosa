//==============================================================================
//
//  FailDependencyPort.java
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


package it.unifi.converter.model.dft.ports;

import java.util.ArrayList;
import java.util.List;

import it.unifi.converter.model.dft.FormatVisitor;
import it.unifi.converter.model.dft.TreeNode;
import it.unifi.converter.model.dft.TreeNodeVisitor;

public class FailDependencyPort extends TreePort {

    private TreeNode triggerInput;
    private List<TreeNode> affectedNodes;
    
    public FailDependencyPort(String name) {
        super(name);
    }

    @Override
    public void setInputs(List<TreeNode> inputs) {
        if(inputs.size()< 2)
            throw new IllegalArgumentException("Fail dependency ports need to have at least 2 inputs");
        
        triggerInput = inputs.get(0);
        affectedNodes = new ArrayList<>();
        affectedNodes.addAll(inputs.subList(1, inputs.size()));
    }

    public TreeNode getTriggerInput() {
        return triggerInput;
    }

    public List<TreeNode> getAffectedNodes() {
        return affectedNodes;
    }
    
    @Override
    public void accept(TreeNodeVisitor v) {
        v.visit(this);
        
    }

    @Override
    public List<TreeNode> getInputs() {
        List<TreeNode> newList = new ArrayList<>();
        newList.add(triggerInput);
        newList.addAll(affectedNodes);
        return newList;
    }
    
    @Override
    public String format(FormatVisitor v) {
        return v.visit(this);
    }


}
