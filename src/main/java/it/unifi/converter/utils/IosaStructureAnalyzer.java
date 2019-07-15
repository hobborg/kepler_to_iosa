//==============================================================================
//
//  IosaStructureAnalyzer.java
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


package it.unifi.converter.utils;
/**
 * This class allows to verify if the model is suitable to be studied with
 * rare event simulation technique based on splitting.
 * Given a IOSA, it measures: 
 * - the minimum distance between the initial state and the failure event
 * - taking into account only the minimum distance paths:
 *     - probability of the path
 *     - minimum probability of a transition
 *     
 * Ideally we want:
 * - long paths
 * - very low path probability
 * - path probability composed by not to low single transition probability
 * 
 * @author Marco Biagi
 *
 */
public class IosaStructureAnalyzer {
    
    public static void main(String[] args) {
        
    }

}
