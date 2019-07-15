//==============================================================================
//
//  DFT.java
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

import java.io.IOException;
import java.util.Collection;

import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.ports.FailDependencyPort;
import it.unifi.converter.model.dft.ports.FunctionalDependencyPort;
import it.unifi.converter.model.dft.ports.repairbox.RepairBox;

/**
 * This class represent a DFT and allows to read it from a file or write it to a file.
 * 
 * @author Marco Biagi
 *
 */
public class DFT {

   private TreeNode root;
   private Collection<RepairBox> repairBoxes;
   private Collection<FunctionalDependencyPort> functionalDependencyPorts;
   private Collection<FailDependencyPort> failDependencyPorts;
  
   
   DFT(TreeNode root, Collection<RepairBox> repairBoxes, Collection<FunctionalDependencyPort> functionalDependencyPorts, Collection<FailDependencyPort> failDependencyPorts){
       this.root = root;
       this.repairBoxes = repairBoxes;
       this.functionalDependencyPorts = functionalDependencyPorts;
       this.failDependencyPorts = failDependencyPorts;
   }
    
   public TreeNode getRoot(){
       return root;
   }
 
   public Collection<RepairBox> getRepairBoxes() {
       return repairBoxes;
   }
   
   public Collection<FunctionalDependencyPort> getFunctionalDependencyPorts() {
       return functionalDependencyPorts;
   }

   public Collection<FailDependencyPort> getFailDependencyPorts() {
       return failDependencyPorts;
   }

   public void toFile(String galileoFilePath){
       GalileoFiles.toGalileoFile(galileoFilePath, this);
   }
   
   public static DFT fromFile(String galileoFilePath) throws IOException, MalformedGalileoFileException{
       return GalileoFiles.fromGalileoFile(galileoFilePath);
   }

   @Override
    public String toString() {
        return "DFT [root=" + root + ", repairBoxes=" + repairBoxes + ", functionalDependencyPorts=" + functionalDependencyPorts
                + ", failDependencyPorts=" + failDependencyPorts + "]";
    }
      
}
