//==============================================================================
//
//  MaxImportanceEvaluator.java
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

import java.math.BigDecimal;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class MaxImportanceEvaluator {
    
    private static final String BE_VALUE = "1";
    private static final String PAND_VALUE = "3";
    private static ScriptEngine engine;
    
    static {
        ScriptEngineManager mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("JavaScript");
    }

    public static int evaluate(String expression){
        //Remove underscores
        String functionStr = expression.replace("_", "");
        
        //Assign value to BE
        while(functionStr.indexOf("BE") != -1){
            int pos = functionStr.indexOf("BE");
            int stopPos = -1;
            for (int i = pos+2; i < functionStr.length(); i++) {
                if(Character.isDigit(functionStr.charAt(i))){
                    continue;
                }
                stopPos = i-1;
                break;
            }
            if(stopPos == -1){
                if(Character.isDigit(functionStr.charAt(functionStr.length() - 1))){//Special case, last char is a digit
                    functionStr = functionStr.substring(0, pos) + BE_VALUE;
                }else{
                    throw new InternalError("Unable to evaluate the max of the importance function for expression: " + expression + " pre-parsing error");
                }
            }else{
                functionStr = functionStr.substring(0, pos) + BE_VALUE + functionStr.substring(stopPos +1);
            }
        }
        
        //Assign value to PAND
        while(functionStr.indexOf("PAND") != -1){
            int pos = functionStr.indexOf("PAND");
            int stopPos = -1;
            for (int i = pos+4; i < functionStr.length(); i++) {
                if(Character.isDigit(functionStr.charAt(i)))
                    continue;
                stopPos = i-1;
                break;
            }
            if(stopPos == -1)
                throw new InternalError("Unable to evaluate the max of the importance function for expression: " + expression + " pre-parsing error");
            
            functionStr = functionStr.substring(0, pos) + PAND_VALUE + functionStr.substring(stopPos +1);
        }
        
        //Assign value to SPARE
        while(functionStr.indexOf("SPARE") != -1){
            //Find token to be replaced
            int pos = functionStr.indexOf("SPARE");
            int stopPos = -1;
            for (int i = pos+5; i < functionStr.length(); i++) {
                if(Character.isDigit(functionStr.charAt(i)))
                    continue;
                stopPos = i-1;
                break;
            } 
            if(stopPos == -1)
                throw new InternalError("Unable to evaluate the max of the importance function for expression: " + functionStr + " pre-parsing error");
            
            //Find value to replace the token
            int maxValue;
            int maxValueStopPos = -1;
            for (int i = stopPos + 3; i < functionStr.length(); i++) {
                if(Character.isDigit(functionStr.charAt(i)))
                    continue;
                maxValueStopPos = i;
                break;
            }
            maxValue = Integer.parseInt(functionStr.substring(stopPos + 3, maxValueStopPos));
            
            //Replace it
            functionStr = functionStr.substring(0, pos) + maxValue + functionStr.substring(stopPos +1);
        }

        //Replace function with javascript functions
        functionStr = functionStr.replaceAll("summax", "sumKHigher");
        functionStr = functionStr.replaceAll("max", "Math.max");
        functionStr = functionStr.replaceAll("min", "Math.min");
        
        //Add a function that allows to evaluate the special sumKHigher function
        functionStr = "function sumKHigher() { "
                + "var args = Array.prototype.slice.call(arguments);"
                + "var k = args[0];"
                + "args.shift();"
                + "args = args.sort();"
                + "var sum = 0;"
                + "for(var i = args.length - k; i < args.length; i++){"
                + "  sum+= args[i];"
                + "}"
                + "return sum;"
                + "};" + functionStr;
        
        try {
            //Force to integer
            String evaluation = engine.eval(functionStr).toString();
            return new BigDecimal(evaluation).intValue();
        } catch (ScriptException e) {
            throw new InternalError("Unable to evaluate the max of the importance function for expression: " + expression + " post-parsing error: " + e.getMessage());
        }
    }
    
}
