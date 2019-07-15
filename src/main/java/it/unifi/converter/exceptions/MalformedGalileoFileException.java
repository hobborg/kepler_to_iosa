//==============================================================================
//
//  MalformedGalileoFileException.java
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


package it.unifi.converter.exceptions;

public class MalformedGalileoFileException extends Exception{

    private static final long serialVersionUID = 718204524926276077L;
    
    public String message;
    
    public MalformedGalileoFileException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage(){
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MalformedGalileoFileException [message=" + message + "]";
    }

}
