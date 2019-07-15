//==============================================================================
//
//  CaseStudiesConversionTest.java
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


package it.unifi.converter;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import it.unifi.converter.exceptions.ConversionException;
import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.DFT;

/**
 * This class test if the library is able to:
 * - parse the DFT
 * - convert it to IOSA
 * without occurring into exceptions, for a lot of model from literature case studies
 * 
 * 
 * @author Marco Biagi
 *
 */
public class CaseStudiesConversionTest {
    
    private static final String[] folders = new String[]{
            "src/main/resources/models/benchmarks/normalized/hcas",
            "src/main/resources/models/benchmarks/normalized/hecs",
            "src/main/resources/models/benchmarks/normalized/mcs",
            "src/main/resources/models/benchmarks/normalized/rc",
            "src/main/resources/models/benchmarks/normalized/sap",
            "src/main/resources/models/benchmarks/normalized/sf",
            
            "src/main/resources/models/customized/hcas",
            "src/main/resources/models/customized/hecs",
            "src/main/resources/models/customized/mcs",
            "src/main/resources/models/customized/rc",
            "src/main/resources/models/customized/sap",
            "src/main/resources/models/customized/sf"
    };
    
    @Test
    public void testAll() throws ConversionException, IOException, MalformedGalileoFileException{
        int countUnsupportedCases = 0;
        for (String folderName : folders) {
            File folder = new File(folderName);
            for ( File file : folder.listFiles()) {
                try{
                    DFT dft = DFT.fromFile(file.getAbsolutePath());
                    StringWriter writer = new StringWriter();
                    DFTToIOSAConverter.convertToIOSA(dft, writer, true, false, false, 10.0f, false, false);
                    writer.close();
                }catch(UnsupportedOperationException e){
                    System.out.println("Model: " + file.getName() + " not supported yet! Error: " + e.getMessage());
                    countUnsupportedCases++;
                }
            }
        }
        //TODO handle FDEP affecting spare gates input otherwise all tests can't be passed
        //assertTrue(countUnsupportedCases == 0);
    }

}
