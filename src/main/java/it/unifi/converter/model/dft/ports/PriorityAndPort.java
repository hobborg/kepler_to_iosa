//==============================================================================
//
//  PriorityAndPort.java
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

public class PriorityAndPort extends TreePort {
    
    private List<TreeNode> inputs;

    public PriorityAndPort(String name) {
        super(name);
    }

    @Override
    public void setInputs(List<TreeNode> inputs) {
        this.inputs = inputs;
    }

    public List<TreeNode> getInputs() {
        return new ArrayList<>(inputs);
    }
    
    @Override
    public void accept(TreeNodeVisitor v) {
        v.visit(this);
    }
    
    @Override
    public String format(FormatVisitor v) {
        return v.visit(this);
    }

}
