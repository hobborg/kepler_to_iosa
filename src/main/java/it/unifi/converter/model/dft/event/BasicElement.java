//==============================================================================
//
//  BasicEvent.java
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


package it.unifi.converter.model.dft.event;

import it.unifi.converter.model.dft.FormatVisitor;
import it.unifi.converter.model.dft.TreeNode;
import it.unifi.converter.model.dft.TreeNodeVisitor;
import it.unifi.converter.model.dft.event.distributions.Distribution;

public class BasicElement extends TreeNode{
    
    private double dormancyFactor = 0.0;
    private Distribution failureDistribution;
    private Distribution dormancyDistribution;//Distribution when component is dormant
    private Distribution repairDistribution;

    public BasicElement(String name, double dormancyFactor, Distribution failureDistribution, Distribution repairDistribution, Distribution dormancyDistribution) {
        super(name);
        this.dormancyFactor = dormancyFactor;
        if(failureDistribution == null)
            throw new IllegalArgumentException("Failure distribution is mandatory!");
        this.failureDistribution = failureDistribution;
        this.repairDistribution = repairDistribution;
        if(dormancyDistribution != null && dormancyFactor == 0.0){
            this.dormancyDistribution = dormancyDistribution;
        }else if(dormancyDistribution == null && dormancyFactor != 0.0){
            this.dormancyDistribution = failureDistribution.evaluateDormancyVersion(dormancyFactor);
            this.dormancyFactor = 0.0;
        }else if(dormancyDistribution != null && dormancyFactor != 0.0){
            throw new IllegalArgumentException("You can't specify both dormancy factor and dormancy distribution for BE " + name);
        }
    }
    
    public BasicElement(String name, Distribution failureDistribution){
        this(name, 0.0, failureDistribution, null, null);
    }
    
    public BasicElement(String name, Distribution failureDistribution, Distribution repairDistribution) {
        this(name, 0.0, failureDistribution, repairDistribution, null);
    }
    
    public BasicElement(String name, double dormancyFactor, Distribution failureDistribution) {
        this(name, dormancyFactor, failureDistribution, null, null);
    }

    public double getDormancyFactor() {
        return dormancyFactor;
    }

    public Distribution getFailureDistribution() {
        return failureDistribution;
    }

    public Distribution getRepairDistribution() {
        return repairDistribution;
    }

    public Distribution getDormancyDistribution() {
        return dormancyDistribution;
    }

    @Override
    public String toString() {
        return "BasicEvent [name=" + getName() + ", dormancyFactor=" + dormancyFactor + ", failureDistribution=" + failureDistribution + ", dormancyDistribution="
                + dormancyDistribution + ", repairDistribution=" + repairDistribution + "]";
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
