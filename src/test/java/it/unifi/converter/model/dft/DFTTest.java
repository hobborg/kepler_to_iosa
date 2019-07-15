//==============================================================================
//
//  DFTTest.java
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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.ports.OrPort;

public class DFTTest {

    @Test(expected=IOException.class)
    public void fileNotFound() throws IOException, MalformedGalileoFileException {
        DFT.fromFile("src/test/resources/tests/galileo-models/testNotFound.dft");
    }
    
    @Test
    public void loadFromFile() throws IOException, MalformedGalileoFileException {
        DFT dft = DFT.fromFile("src/test/resources/tests/galileo-models/test1.dft");
        assertTrue(dft.getFailDependencyPorts().size() == 0);
        assertTrue(dft.getFunctionalDependencyPorts().size() == 0); 
        assertTrue(dft.getRepairBoxes().size() == 0);
        
        assertEquals(dft.getRoot().getClass(), OrPort.class);
        assertEquals(dft.getRoot().getName(), "OR1");
        
        OrPort orPort = ((OrPort)(dft.getRoot()));
        assertTrue(orPort.getInputs().size() == 2);
    }
    
    @Test
    public void fileWithCommentsAndEmptyLines() throws IOException, MalformedGalileoFileException {
        DFT dft = DFT.fromFile("src/test/resources/tests/galileo-models/testSpecialChars.dft");
        assertTrue(dft.getFailDependencyPorts().size() == 0);
        assertTrue(dft.getFunctionalDependencyPorts().size() == 0); 
        assertTrue(dft.getRepairBoxes().size() == 0);
        
        assertEquals(dft.getRoot().getClass(), OrPort.class);
        assertEquals(dft.getRoot().getName(), "OR1");
        
        OrPort orPort = ((OrPort)(dft.getRoot()));
        assertTrue(orPort.getInputs().size() == 2);
    }

}
