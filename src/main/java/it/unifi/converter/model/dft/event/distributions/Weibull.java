//==============================================================================
//
//  Weibull.java
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


package it.unifi.converter.model.dft.event.distributions;

public class Weibull implements Distribution {

    private double rate;
    private double shape;
    
    public Weibull(double rate, double shape){
        this.rate = rate;
        this.shape = shape;
    }

    public double getRate() {
        return rate;
    }

    public double getShape() {
        return shape;
    }
    
    @Override
    public String asFIGFormatString() {
        throw new UnsupportedOperationException("Not implemented!");
    }
    
    @Override
    public Distribution evaluateDormancyVersion(double dormancyFactor) {
        throw new UnsupportedOperationException("Not yet supported!");
    }
}
