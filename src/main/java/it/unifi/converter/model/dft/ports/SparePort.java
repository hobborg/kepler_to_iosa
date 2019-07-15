//==============================================================================
//
//  SparePort.java
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

public class SparePort extends TreePort {
    
    public enum SpareType{
        cold,
        warm,
        hot
    }

    private TreeNode primaryInput;
    private List<TreeNode> spareInputs;
    private SpareType type;
    
    public SparePort(String name, SpareType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void setInputs(List<TreeNode> inputs) {
        if(inputs.size()< 2)
            throw new IllegalArgumentException("Spare ports need to have at least 2 inputs, for port " + this.getName());
        
        primaryInput = inputs.get(0);
        spareInputs = new ArrayList<>(inputs.subList(1, inputs.size()));
    }

    public TreeNode getPrimaryInput() {
        return primaryInput;
    }

    public List<TreeNode> getSpareInputs() {
        return spareInputs;
    }
    
    public SpareType getType(){
        return type;
    }
    
    @Override
    public void accept(TreeNodeVisitor v) {
        v.visit(this);
    }

    @Override
    public List<TreeNode> getInputs() {
        List<TreeNode> newList = new ArrayList<>();
        newList.add(primaryInput);
        newList.addAll(spareInputs);
        return newList;
    }
    
    @Override
    public String format(FormatVisitor v) {
        return v.visit(this);
    }

    
}
