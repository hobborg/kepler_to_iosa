//==============================================================================
//
//  DFTToIOSAConverterTest.java
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import it.unifi.converter.exceptions.ConversionException;
import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.DFT;

/**
 * In this class we test as many types of conversion as possible. They must give the expected output
 * as result since the conversion is deterministic in all its steps
 * 
 * @author Marco Biagi
 *
 */
public class DFTToIOSAConverterTest {

    /**
     * Tree with only two BE with EXP failure rates and no repairs
     */
    @Test
    public void testBasic() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test1.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test1.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree with only two BE with GEN failures and no repairs
     */
    @Test
    public void testGENFailures() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test2.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test2.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree with only two BE with GEN failures and GEN repairs without RBOX
     */
    @Test
    public void testGENFailuresAndRepairs() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test3.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test3.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree with only two BE with GEN failures and GEN repairs with priority RBOX
     */
    @Test
    public void testGENFailuresAndRepairsPriorityRBOX() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test4.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test4.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree with 4 inputs PAND
     */
    @Test
    public void testPAND() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test5.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test5.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree where top event is an OR gate that has 2 children SPARE and shared BE as spare
     */
    @Test
    public void testSpareGates() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test6.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test6.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree with FDEP
     */
    @Test
    public void testFDEP() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test7.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test7.sa";   
        compareConversionResult(input, expectedOutput);
    }

    /**
     * Tree with FXDEP
     */
    @Test
    public void testFXDEP() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test8.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test8.sa";   
        compareConversionResult(input, expectedOutput);
    }

    /**
     * Tree with dependent basic event
     */
    @Test
    public void testDependentBEs() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test9.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test9.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Test single repair boxes
     */
    @Test
    public void testSingleRBOXes() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test10.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test10.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Tree with FDEP with repairs
     */
    @Test
    public void testFDEPRepairs() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test12.dft";
        String expectedOutput = "src/test/resources/tests/iosac-models/test12.sa";   
        compareConversionResult(input, expectedOutput);
    }
    
    /**
     * Test single repair boxes error
     */
    @Test(expected=MalformedGalileoFileException.class)
    public void testSingleRBOXesError() throws IOException, MalformedGalileoFileException, ConversionException {
        String input = "src/test/resources/tests/galileo-models/test11.dft";
        DFT.fromFile(input);
    }

    
    private static void compareConversionResult(String inputFile, String expectedOutputFile) throws IOException, MalformedGalileoFileException, ConversionException{
        //Read Galileo file
        DFT dft = DFT.fromFile(inputFile);
       
        //In memory representation of conversion result
        StringWriter writer = new StringWriter();
        DFTToIOSAConverter.convertToIOSA(dft, writer, false, false, false, null, false, false);
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
