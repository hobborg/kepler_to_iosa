//==============================================================================
//
//  TopEventPropertyVisitor.java
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

import it.unifi.converter.model.dft.TreeNodeVisitor;
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
 * This class allows, given the top element of a DFT, to build the a property expressed in the
 * FIG language, that represent the occurring of top event:
 * http://dsg.famaf.unc.edu.ar/fig/iosa
 * 
 * To use it, first visit the top node and than invoke the getter.
 * 
 * @author Marco Biagi
 *
 */
public class TopEventPropertyVisitor implements TreeNodeVisitor{

    private String topEventProperty = null;
    private long nodeId;
    
    public TopEventPropertyVisitor(long nodeId){
        this.nodeId = nodeId;
    }
    
    public String getTopEventProperty() {
        if(topEventProperty == null)
            throw new IllegalStateException("In order to evaluate the top event property for the tree, you need first to invoke visit() on the tree root");
        return topEventProperty;
    }

    @Override
    public void visit(BasicEvent n) {
        topEventProperty = "brokenFlag_" + nodeId + " > 0";
    }

    @Override
    public void visit(AndPort n) {
        topEventProperty = "count_" + nodeId + "==" + n.getInputs().size();
    }

    @Override
    public void visit(FailDependencyPort n) {
        throw new UnsupportedOperationException("Fail dependency port can't be the root of the tree!");
    }
    
    @Override
    public void visit(FunctionalDependencyPort n) {
        throw new UnsupportedOperationException("Functional dependency port can't be the root of the tree!");
    }

    @Override
    public void visit(KOfNPort n) {
        topEventProperty = "count_" + nodeId + "==" + n.getK();
    }

    @Override
    public void visit(OrPort n) {
        topEventProperty = "count_" + nodeId + ">0";
    }

    @Override
    public void visit(PriorityAndPort n) {
        topEventProperty = "st_" + nodeId + "==2";
    }

    @Override
    public void visit(SparePort n) {
        topEventProperty = "state_" + nodeId + "==4";
    }

    @Override
    public void visit(RepairBox n) {
        throw new IllegalArgumentException("Repair box can't be the root of the tree!");
    }

}
