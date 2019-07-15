//==============================================================================
//
//  CompositionalImportanceFunctionFIGTest.java
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import it.unifi.converter.DFTToIOSAConverter;
import it.unifi.converter.exceptions.ConversionException;
import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.DFT;

public class CompositionalImportanceFunctionFIGTest {

    @Test
    public void testPAND() throws IOException, MalformedGalileoFileException, ConversionException {
        String inputFile = "src/test/resources/tests/galileo-models/testGIF1.dft";
        String cif = evaluateCompositionalImportanceFunction(inputFile);
        System.out.println(cif); 
        String expectedFunciton = "max(0,6.0*PAND_7+2*(BE_0+BE_1+BE_2)+(PAND_7>0?3*(BE_4+BE_5):-3*(BE_4+BE_5)));0;30";
        assertEquals(cif, expectedFunciton);
    }
    
    @Test
    public void testSPARE() throws IOException, MalformedGalileoFileException, ConversionException {
        String inputFile = "src/test/resources/tests/galileo-models/testGIF2.dft";
        String cif = evaluateCompositionalImportanceFunction(inputFile);
        System.out.println(cif);
        String expectedFunciton = "max(BE_0+BE_1+BE_2+3*(BE_4)+3*(BE_7),(10.0*(SPARE_5==7? 1 : 0)));0;10";
        assertEquals(cif, expectedFunciton);
    }
    
    @Test
    public void testKOfN() throws IOException, MalformedGalileoFileException, ConversionException {
        String inputFile = "src/test/resources/tests/galileo-models/testGIF3.dft";
        String cif = evaluateCompositionalImportanceFunction(inputFile);
        System.out.println(cif);
        String expectedFunciton = "summax(4,BE_0+BE_1+BE_2,3*(BE_4),3*(BE_5),3*(BE_6),3*(BE_7),3*(BE_8));0;12";
        assertEquals(cif, expectedFunciton);
    }

    private static String evaluateCompositionalImportanceFunction(String inputFile) throws IOException, MalformedGalileoFileException, ConversionException{
        String iosac = convertToIOSAC(inputFile);
        String[] lines = iosac.split("\\n");
        String cif = lines[lines.length -1];
        return cif.substring(3);
    }
    
    private static String convertToIOSAC(String inputFile) throws IOException, MalformedGalileoFileException, ConversionException{
        DFT dft = DFT.fromFile(inputFile);
        StringWriter writer = new StringWriter();
        DFTToIOSAConverter.convertToIOSA(dft, writer, false, false, false, null, true, true);
        writer.close();
        return writer.toString();
    }
    
}
