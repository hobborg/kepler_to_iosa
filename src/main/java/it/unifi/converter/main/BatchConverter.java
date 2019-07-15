//==============================================================================
//
//  BatchConverter.java
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


package it.unifi.converter.main;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Convert all files in specified folder to iosa-c file
 * 
 * @author Marco Biagi
 */
public class BatchConverter {
    public static void main(String[] args) throws Exception {
        
        class Dataset{
            
            public Dataset(String folder, Float timeLimit, String placeHolderOriginalFolder, String newStringFolder){
                this.folder = folder;
                this.timeLimit = timeLimit;
                this.placeHolderOriginalFolder = placeHolderOriginalFolder;
                this.newStringFolder = newStringFolder;
            }
            public String folder;
            public Float timeLimit;
            public String placeHolderOriginalFolder;
            public String newStringFolder;
        }
        
        //In any case we check the reliability property after one years, so we define the value of the property according to the time unit
        List<Dataset> datasets = Arrays.asList(
//                new Dataset("src/main/resources/models/customized/hcas", 8760.0f),//hours
//                new Dataset("src/main/resources/models/customized/hecs", 8760.0f),//hours
//                new Dataset("src/main/resources/models/customized/mcs", 1.0f),//years
//                new Dataset("src/main/resources/models/customized/rc", 1.0f),//years
//                new Dataset("src/main/resources/models/customized/sap", 365.0f),//days
//                new Dataset("src/main/resources/models/customized/sf", 1.0f)//years
                
                //new Dataset("src/main/resources/models/customized-for-isplit/sap", 365.0f)//days
                
                new Dataset("src/main/resources/models/generated/hecs/", 8760.0f, "generated", "generated-iosac"),//hours
                new Dataset("src/main/resources/models/generated-increased/hecs/hecs1", 8760.0f, "generated-increased", "generated-increased-iosac"),//hours
                new Dataset("src/main/resources/models/generated-increased/hecs/hecs2", 8760.0f, "generated-increased", "generated-increased-iosac"),//hours
                new Dataset("src/main/resources/models/generated-increased/hecs/hecs3", 8760.0f, "generated-increased", "generated-increased-iosac"),//hours
                new Dataset("src/main/resources/models/generated-increased/hecs/hecs4", 8760.0f, "generated-increased", "generated-increased-iosac"),//hours
                new Dataset("src/main/resources/models/generated-increased/hecs/hecs5", 8760.0f, "generated-increased", "generated-increased-iosac"),//hours
                
                new Dataset("src/main/resources/models/customized/mcs/", 1.0f, "customized", "customized-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/mcs/mcs1", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/mcs/mcs2", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/mcs/mcs3", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/mcs/mcs4", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/mcs/mcs5", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                
                new Dataset("src/main/resources/models/customized/rc/", 1.0f, "customized", "customized-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/rc/rc1", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/rc/rc2", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/rc/rc3", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/rc/rc4", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/rc/rc5", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                
                new Dataset("src/main/resources/models/customized/sap/", 365.0f, "customized", "customized-iosac"),//days
                new Dataset("src/main/resources/models/customized-increased/sap/sap1", 365.0f, "customized-increased", "customized-increased-iosac"),//days
                new Dataset("src/main/resources/models/customized-increased/sap/sap2", 365.0f, "customized-increased", "customized-increased-iosac"),//days
                new Dataset("src/main/resources/models/customized-increased/sap/sap3", 365.0f, "customized-increased", "customized-increased-iosac"),//days
                new Dataset("src/main/resources/models/customized-increased/sap/sap4", 365.0f, "customized-increased", "customized-increased-iosac"),//days
                new Dataset("src/main/resources/models/customized-increased/sap/sap5", 365.0f, "customized-increased", "customized-increased-iosac"),//days

                new Dataset("src/main/resources/models/customized/sf/", 1.0f, "customized", "customized-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/sf/sf1", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/sf/sf2", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/sf/sf3", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/sf/sf4", 1.0f, "customized-increased", "customized-increased-iosac"),//years
                new Dataset("src/main/resources/models/customized-increased/sf/sf5", 1.0f, "customized-increased", "customized-increased-iosac")//years
                
                );
        
//        String originalFolder ="customized";
//        String outputFolder ="customized-iosac";
//        
//        String originalFolder ="customized-for-isplit";
//        String outputFolder ="customized-for-isplit-iosac";
   
        for (Dataset dataset : datasets) {
            File folder = new File(dataset.folder);
            for ( File file : folder.listFiles()) {
                try{
                    String inputFilePath = file.getAbsolutePath();
                    String outputFilePath = file.getAbsolutePath().replace(".dft", ".sa").replace(dataset.placeHolderOriginalFolder, dataset.newStringFolder);
                    boolean addReliabilityProperty = true;
                    boolean addStateReachabilityProperty = true;
                    boolean addAvailabilityProperty = true;
                    Float timeLimit = dataset.timeLimit;
                    
                    Launcher.convert(inputFilePath, outputFilePath, addReliabilityProperty, addStateReachabilityProperty, addAvailabilityProperty, timeLimit, true, true);
                }catch(UnsupportedOperationException e){
                    System.out.println("Skipping " + file.getName() + ", unable to convert it: " + e.getMessage());
                }
            }
        }
    }
}
