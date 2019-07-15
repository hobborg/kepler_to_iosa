//==============================================================================
//
//  CompositionalImportanceFunctionFIG.java
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import it.unifi.converter.model.dft.FormatVisitor;
import it.unifi.converter.model.dft.TreeNode;
import it.unifi.converter.model.dft.event.BasicEvent;
import it.unifi.converter.model.dft.ports.AndPort;
import it.unifi.converter.model.dft.ports.FailDependencyPort;
import it.unifi.converter.model.dft.ports.FunctionalDependencyPort;
import it.unifi.converter.model.dft.ports.KOfNPort;
import it.unifi.converter.model.dft.ports.OrPort;
import it.unifi.converter.model.dft.ports.PriorityAndPort;
import it.unifi.converter.model.dft.ports.SparePort;
import it.unifi.converter.model.dft.ports.repairbox.RepairBox;
import it.unifi.converter.utils.MaxImportanceEvaluator;

/**
 * Generate the compositional importance function
 * 
 * @author Marco Biagi
 *
 */
public class CompositionalImportanceFunctionFIG  implements FormatVisitor{

    private Map<TreeNode, Long> assignedId;
    private boolean normalizeInputs;
    private static final String AND_OPERATOR = "+";
    private static final String FUNCTION_ARGUMENT_SEPARATOR = ",";
    private static final String MAX_FUNCTION = "max";
    private static final String MIN_FUNCTION = "min";
    private static final double PAND_FUNCTION_CONSTANT = 1;
    private static final String BE_PREFIX = "BE_";
    private static final String PAND_PREFIX = "PAND_";
    private static final String SPARE_PREFIX = "SPARE_";
    private static final String SUM_K_MAX_FUNCTION = "summax";
    
    public CompositionalImportanceFunctionFIG(Map<TreeNode, Long> assignedId, boolean normalizeInputs){
        this.assignedId = assignedId;
        this.normalizeInputs = normalizeInputs;
    }
    
    @Override
    public String visit(BasicEvent n) {
        return BE_PREFIX + assignedId.get(n);
    }

    @Override
    public String visit(AndPort n) {
        List<String> args = n.getInputs().stream().map( input -> input.format(this) ).collect( Collectors.toList() );
        if(normalizeInputs)
            args = normalizeChildren(args);
        return binaryOperation(AND_OPERATOR, args);        
    }
    
    @Override
    public String visit(FunctionalDependencyPort n) {
        throw new UnsupportedOperationException("Invalid method invocation: FXDEP should be not taken into account in compositional importance function");
    }

    @Override
    public String visit(FailDependencyPort n) {
        throw new UnsupportedOperationException("Invalid method invocation: FDEP should be not taken into account in compositional importance function");
    }

    //summax(BE1, BE2, ..., BEN, k)
    @Override
    public String visit(KOfNPort n) {
        List<String> args = n.getInputs().stream().map( input -> input.format(this) ).collect( Collectors.toList() );
        int k = n.getK();
        if(normalizeInputs){
            args = normalizeChildren(args);
        }
        return summax(args, k);
    }

    @Override
    public String visit(OrPort n) {
        List<String> args = n.getInputs().stream().map( input -> input.format(this) ).collect( Collectors.toList() );
        if(normalizeInputs)
            args = normalizeChildren(args);
        return max(args);
    }

    @Override
    public String visit(PriorityAndPort n) {
        if(n.getInputs().size() !=2)
            throw new UnsupportedOperationException("PAND " + n.getName() + " has more than 2 inputs!");
        
        String pandName = PAND_PREFIX + assignedId.get(n);
        String firstInput = n.getInputs().get(0).format(this);
        String secondInput = n.getInputs().get(1).format(this);
        
        double gateMultiplier = PAND_FUNCTION_CONSTANT;
        if(normalizeInputs){
            int lcm = getLCMChildren(new ArrayList<>(Arrays.asList(firstInput, secondInput)));
            gateMultiplier = lcm; //Gate multiplier is the lcm, this guarantee that gate state is more important that single inputs
            //First input
            int firstMax = MaxImportanceEvaluator.evaluate(firstInput);
            firstInput = addMultiplier(lcm/firstMax, firstInput);
            //Second input
            int secondMax = MaxImportanceEvaluator.evaluate(secondInput);
            secondInput = addMultiplier(lcm/secondMax, secondInput);
        }else {
            //Gate multiplier is the maximum between the maximum contribution of inputs
            int firstMax = MaxImportanceEvaluator.evaluate(firstInput);
            int secondMax = MaxImportanceEvaluator.evaluate(secondInput);
            gateMultiplier = Math.max(firstMax, secondMax);
        }
        
        return pandCompositionFunction(gateMultiplier, firstInput, secondInput, pandName);
    }

    @Override
    public String visit(SparePort n) {
        
        String spareName = SPARE_PREFIX + assignedId.get(n);
        
        List<String> args = n.getInputs().stream().map( input -> input.format(this) ).collect( Collectors.toList() );
        int gateMultiplier;
        if(normalizeInputs) {
            int lcm = getLCMChildren(args);
            gateMultiplier = (lcm * args.size()) + 1; //This guarantee that gate state is more important that all inputs
            args = normalizeChildren(args);
        }else {
            
            Map<String, Integer> evaluations = new HashMap<String, Integer>();
            for (String arg : args) {
                evaluations.put(arg, MaxImportanceEvaluator.evaluate(arg));
            }
            int max = evaluations.values().stream().mapToInt(i -> i.intValue()).sum();
            gateMultiplier = max +1;
        }
        
        return spareCompositionFunction(gateMultiplier, args, spareName);
        
    }

    @Override
    public String visit(RepairBox n) {
        throw new UnsupportedOperationException("Invalid method invocation: RBOX should be not taken into account in compositional importance function");
        
    }
    
    private static String binaryOperation(String operator, List<String> args){
        return StringUtils.join(args, operator);
    }
    
    private static String functionWithArguments(String function, List<String> args){
        String result = function+"(";
        result+=StringUtils.join(args, FUNCTION_ARGUMENT_SEPARATOR);
        result+=")";
        return result;
    }

    private static String max(List<String> args){
        return functionWithArguments(MAX_FUNCTION, args);
    }
    
//    private static String min(List<String> args){
//        return functionWithArguments(MIN_FUNCTION, args);
//    }
    
    private static String summax(List<String> args, int k){
        List<String> arguments = new ArrayList<>(args);
        arguments.add(0,"" + k);
        return functionWithArguments(SUM_K_MAX_FUNCTION, arguments);
    }
    
    //@deprecated: old version
//    private static String pandCompositionFunction(double gateMultiplier, String firstInput, String secondInput, String gateName){
//        String result ="";
//        result+= gateMultiplier + "*" + gateName;
//        result+= "+" + firstInput;
//        result+= "+(" + secondInput + "/";
//        result+= MAX_FUNCTION + "(1, (" + secondInput + ")-("+ firstInput + "))";   
//        result+= ")";
//        return result;
//    }
    
    public static void main(String[] args) {
        System.out.println(pandCompositionFunction(2.0, "A", "B", "G1"));
    }
    
    //max(0, lcm * G + A + (G>0? B : -B))
    private static String pandCompositionFunction(double gateMultiplier, String firstInput, String secondInput, String gateName){
        String result = ""; 
        result += MAX_FUNCTION + "(0,";
        result += gateMultiplier + "*" + gateName;
        result+= "+" + firstInput;
        result+= "+(" + gateName + ">0?";
        result+= secondInput;
        result+= ":";
        result+= "-" + secondInput;
        result+= "))";
                
        return result;
    }
    
    private static String spareCompositionFunction(double gateMultiplier, List<String> inputs, String gateName) {
        int maxValue = (inputs.size() * 2) + 1;//This is the maximum importance of the component according to FIG distance evaluation 
        String result = "";
        result += MAX_FUNCTION + "(";
        result += binaryOperation(AND_OPERATOR, inputs);
        result +=",(" + gateMultiplier;
        result += "*(" + gateName + "=="+maxValue+"? 1 : 0)))";
        return result;
    }
    
    private static int getLCMChildren(List<String> children){
        //1- Evaluate children max value
        Map<String, Integer> evaluations = new HashMap<String, Integer>();
        for (String arg : children) {
            evaluations.put(arg, MaxImportanceEvaluator.evaluate(arg));
        }
        
        //2- Evaluate lcm
        int lcm = intLeastCommonMultiple(evaluations.values());
        
        return lcm;
    }
    
    private static String addMultiplier(int multiplier, String element){
        if(multiplier == 1)
            return element;
        else
            return multiplier + "*(" + element + ")";
        
    }
    
    private static List<String> normalizeChildren(List<String> children){
        //1- Evaluate children max value
        Map<String, Integer> evaluations = new HashMap<String, Integer>();
        for (String arg : children) {
            evaluations.put(arg, MaxImportanceEvaluator.evaluate(arg));
        }
        
        //2- Evaluate lcm
        int lcm = intLeastCommonMultiple(evaluations.values());
        
        //3- Add multiplier to each children
        List<String> argsWithMultiplier = new ArrayList<>();
        for (String arg : children) {
            int multiplier = lcm/evaluations.get(arg);
            argsWithMultiplier.add(addMultiplier(multiplier, arg));
        }
        return argsWithMultiplier;
    }

    
    private static int intGCD(int x, int y){
        BigInteger b1 = BigInteger.valueOf(x);
        BigInteger b2 = BigInteger.valueOf(y);
        BigInteger gcd = b1.gcd(b2);
        return gcd.intValue();
    }

    private static int intLeastCommonMultiple(int x, int y){
        int mcm = 1;
        int f = intGCD(x, y);
        mcm = x*y/f;
        return mcm;
    }
    
    private static int intLeastCommonMultiple(Collection<Integer> values){
        Iterator<Integer> iterator = values.iterator();
        int result = iterator.next();
        while(iterator.hasNext()){
            result = intLeastCommonMultiple(result, iterator.next());
        }
        return result;
    }
    
}
