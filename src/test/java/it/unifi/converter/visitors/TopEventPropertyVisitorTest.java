//==============================================================================
//
//  TopEventPropertyVisitorTest.java
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
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import it.unifi.converter.DFTToIOSAConverter;
import it.unifi.converter.exceptions.ConversionException;
import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.DFT;

/**
 * 
 * In this class we test the creation of the top event property.
 * We check all possible cases of supported top event gate (since the reliability and the
 * availability properties are generated in a different way depending on the top event gate)
 * 
 * @author Marco Biagi
 *
 */
public class TopEventPropertyVisitorTest {

    @Test
    public void testTE_AND() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testREL1.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testREL1.sa";   
        compareConversionResult(input, expectedOutput, true, false);
    }
    
    @Test
    public void testTE_OR() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testREL2.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testREL2.sa";   
        compareConversionResult(input, expectedOutput, true, false);
    }
    
    @Test
    public void testTE_BasicEvent() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testREL3.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testREL3.sa";   
        compareConversionResult(input, expectedOutput, true, false);
    }
    
    @Test
    public void testTE_KofN() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testREL4.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testREL4.sa";   
        compareConversionResult(input, expectedOutput, true, false);
    }
    
    @Test
    public void testTE_PAND() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testREL5.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testREL5.sa";   
        compareConversionResult(input, expectedOutput, true, false);
    }
    
    @Test
    public void testTE_SPARE() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testREL6.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testREL6.sa";   
        compareConversionResult(input, expectedOutput, true, false);
    }
    
    @Test
    public void testTE_AND_BothProps() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/testBOTH1.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/testBOTH1.sa";   
        compareConversionResult(input, expectedOutput, true, true);
    }
    
    private static void compareConversionResult(String inputFile, String expectedOutputFile, boolean rel, boolean avail) throws IOException, MalformedGalileoFileException, ConversionException{
        //Read Galileo file
        DFT dft = DFT.fromFile(inputFile);
       
        //In memory representation of conversion result
        StringWriter writer = new StringWriter();
        DFTToIOSAConverter.convertToIOSA(dft, writer, rel, false, avail, new Float(5.5), false, false);
        writer.close();
        String result = writer.toString();
        System.out.println(result);
        //In memory representation of expected result
        byte[] encoded = Files.readAllBytes(Paths.get(expectedOutputFile));
        String expectedOutput = new String(encoded, "UTF-8");
        
        //Compare
        assertEquals(result, expectedOutput);        
    }

}
