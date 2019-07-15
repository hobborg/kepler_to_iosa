//==============================================================================
//
//  GalileoFiles.java
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import it.unifi.converter.exceptions.MalformedGalileoFileException;
import it.unifi.converter.model.dft.event.BasicEvent;
import it.unifi.converter.model.dft.event.distributions.Distribution;
import it.unifi.converter.model.dft.event.distributions.Exponential;
import it.unifi.converter.model.dft.event.distributions.LogNormal;
import it.unifi.converter.model.dft.event.distributions.OtherDistribution;
import it.unifi.converter.model.dft.event.distributions.Weibull;
import it.unifi.converter.model.dft.ports.AndPort;
import it.unifi.converter.model.dft.ports.FailDependencyPort;
import it.unifi.converter.model.dft.ports.FunctionalDependencyPort;
import it.unifi.converter.model.dft.ports.KOfNPort;
import it.unifi.converter.model.dft.ports.OrPort;
import it.unifi.converter.model.dft.ports.PriorityAndPort;
import it.unifi.converter.model.dft.ports.TreePort;
import it.unifi.converter.model.dft.ports.SparePort;
import it.unifi.converter.model.dft.ports.SparePort.SpareType;
import it.unifi.converter.model.dft.ports.repairbox.RepairBox;

/**
 * This class allows to read a DFT from a Galileo file and write a DFT to a Galileo file.
 * 
 * Galileo specification:
 * https://www.cse.msu.edu/~cse870/Materials/FaultTolerant/manual-galileo.htm
 * 
 * @author Marco Biagi
 *
 */
class GalileoFiles {
    
    private enum NodeType {
        and, or, hsp, wsp, csp, pand, seq, xofy, fdep, be, //Original Galileo format
        fxdep, repairbox_fcfs, repairbox_priority, repairbox_random, repairbox_single //Extensions
    }
    private static final String TOPLEVEL_LABEL = "toplevel";
    private static final String DORMANCY_PARAMETER_LABEL = "dorm";
    private static final String EXP_LAMBDA_PARAMETER_LABEL = "lambda";//EXP
    private static final String PROB_PARAMETER_LABEL = "prob";//Discrete probability distribution
    private static final String WEIBULL_RATE_PARAMETER_LABEL = "rate";//Weibull
    private static final String WEIBULL_SHAPE_PARAMETER_LABEL = "shape";//Weibull
    private static final String LOGNORMAL_MEAN_PARAMETER_LABEL = "mean";//Log-normal
    private static final String LOGNORMAL_SD_PARAMETER_LABEL = "stddev";//Log-normal
    //Extensions
    private static final String EXTENSION_FAIL_PDF = "EXT_failPDF";
    private static final String EXTENSION_REPAIR_PDF = "EXT_repairPDF";
    private static final String EXTENSION_DORMANCY_PDF = "EXT_dormPDF";
    

    /**---------------------------------------------------------------------------------------------------------------
     * SAVE TO FILE 
     *--------------------------------------------------------------------------------------------------------------*/
    
    /**
     * Save DFT to specified location in the Galileo format
     */
    public static void toGalileoFile(String galileoFilePath, DFT dft){
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**---------------------------------------------------------------------------------------------------------------
     * READ FROM FILE 
     *--------------------------------------------------------------------------------------------------------------*/
    
    /**
     * Given the path of a DFT specified in a Galileo format, create an in memory representation of the DFT
     * and return its root
     * @param path of the Galileo file
     * @return root of the in memory representation of the DFT
     * @throws IOException 
     * @throws MalformedGalileoFileException 
     */
    public static DFT fromGalileoFile(String galileoFilePath) throws IOException, MalformedGalileoFileException{
        //1- Create all nodes of the tree without links
        Map<String, TreeNode> mapNodes = buildNodesWithoutLinks(galileoFilePath);
        //2- Add links between nodes
        addLinks(galileoFilePath, mapNodes);
        //3- Find the root
        TreeNode root = findRoot(galileoFilePath, mapNodes);
        //4- Find all repairBoxes
        Collection<RepairBox> repairBoxes = findRepairBoxes(mapNodes);
        //5- Find all functional dependency port
        Collection<FunctionalDependencyPort> functionalDependencyPorts = findFunctionalDependency(mapNodes);
        //6- Find all fail dependency port
        Collection<FailDependencyPort> failDependencyPorts = findFailDependency(mapNodes);
        //7- Check the structure of the tree
        checkStructure(root);
        //8- Check repair boxes
        checkRBOX(repairBoxes);
        //9- Build the wrapper
        return new DFT(root, repairBoxes, functionalDependencyPorts, failDependencyPorts);
    }
    
    /**
     * Do some basic check on parsed DFT to verify if some error is present or some unsupported "scenarios" are present 
     */
    private static void checkStructure(TreeNode root) {
        //Visit tree structure to verify if a spare gate with gate as input spare is present
        Deque<TreeNode> toBeVisited = new ArrayDeque<>();
        toBeVisited.add(root);
        while(!toBeVisited.isEmpty()){
            TreeNode current = toBeVisited.pollLast();
            if(current instanceof SparePort){
                SparePort spareGate = (SparePort)current;
                for (TreeNode treeNode : spareGate.getSpareInputs()) {
                    if(!(treeNode instanceof BasicEvent)){
                        throw new UnsupportedOperationException("Spare gate, with spare inputs not basic event not supported.  Gate: " + current.getName() + " input: " + treeNode.getName());
                    }
                }
                toBeVisited.add(spareGate.getPrimaryInput());
            }else if(current instanceof TreePort){
                toBeVisited.addAll(((TreePort)current).getInputs());
            }
        }        
    }
    
    /**
     * Do basic check on repair boxes
     * 
     * @throws MalformedGalileoFileException 
     */
    private static void checkRBOX(Collection<RepairBox> repairBoxes) throws MalformedGalileoFileException {
        for (RepairBox repairBox : repairBoxes) {
            if(repairBox.getInputs().size() == 0)
                throw new MalformedGalileoFileException("RBOX " + repairBox.getName() + " has no associated BE to be repaired");
            if(repairBox.getPolicy().equals(RepairBox.Policy.single) && repairBox.getInputs().size() != 1)
                throw new MalformedGalileoFileException("single RBOX " + repairBox.getName() + " must have only a single associated BE");
        }
    }

    private static List<RepairBox> findRepairBoxes(Map<String, TreeNode> mapNodes){
        return mapNodes.values().stream()
        .filter(sc -> sc instanceof RepairBox)
        .map (sc -> (RepairBox) sc)
        .collect(Collectors.toList());
    }
    
    private static List<FunctionalDependencyPort> findFunctionalDependency(Map<String, TreeNode> mapNodes){
        return mapNodes.values().stream()
        .filter(sc -> sc instanceof FunctionalDependencyPort)
        .map (sc -> (FunctionalDependencyPort) sc)
        .collect(Collectors.toList());
    }
    
    private static List<FailDependencyPort> findFailDependency(Map<String, TreeNode> mapNodes){
        return mapNodes.values().stream()
        .filter(sc -> sc instanceof FailDependencyPort)
        .map (sc -> (FailDependencyPort) sc)
        .collect(Collectors.toList());
    }
    
    private static Map<String, TreeNode> buildNodesWithoutLinks(String galileoFilePath) throws IOException, MalformedGalileoFileException{
        Map<String, TreeNode> mapNodes = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(galileoFilePath))) {
            boolean first = true;
            String line;
            while ((line = br.readLine()) != null) {
                line = removeCommentIfPresents(line);
                if(skipLine(line))
                    continue;
                
                if(first){//Skip first line since indicate only which is the root
                    first = false;
                    continue;
                }
                
                TreeNode node = buildTreeNodeWithoutLinks(line);
                if(mapNodes.containsKey(node.getName()))
                    throw new MalformedGalileoFileException("There are 2 node with the same name! Unable to parse DFT file"); 
                mapNodes.put(node.getName(), node);
            }

        } catch (IOException e) {
            throw e;
        }
        return mapNodes;
    }
    
    private static String removeCommentIfPresents(String line){
        int startCommentIndex = line.indexOf("/*");
        int endCommentIndex = line.indexOf("*/", startCommentIndex);
        if(startCommentIndex != -1){
            if(endCommentIndex == -1)
                throw new UnsupportedOperationException("Multiline comments are not yet supported!");
            else
                return (line.substring(0, startCommentIndex)) +  (line.substring(endCommentIndex +2));
        }
        return line;
    }
    
    private static boolean skipLine(String line){
      //If empty line, skip it
        if(line.trim().equals(""))
            return true;
        return false;
    }
    
    private static void addLinks(String galileoFilePath, Map<String, TreeNode> mapNodes) throws IOException, MalformedGalileoFileException{
        try (BufferedReader br = new BufferedReader(new FileReader(galileoFilePath))) {
            boolean first = true;
            String line;
            while ((line = br.readLine()) != null) {
                line = removeCommentIfPresents(line);
                if(skipLine(line))
                    continue;
                if(first){//Skip first line since indicate only which is the root
                    first = false;
                    continue;
                }
                String name = getName(line);
                NodeType type = getType(line);
                
                //Basic event doesn't have inputs
                if(type.equals(NodeType.be))
                    continue;
                
                List<String> inputNames = getInputNames(line);
                List<TreeNode> inputs = new ArrayList<>();
                for (String inputName : inputNames) {
                    if(mapNodes.get(inputName) == null)
                        throw new MalformedGalileoFileException("Component " + inputName + " is specified as input of port " + name + " but is not defined");
                    inputs.add(mapNodes.get(inputName));
                }
                ((TreePort)(mapNodes.get(name))).setInputs(inputs);
            }

        } catch (IOException e) {
            throw e;
        }
    }
    
    private static List<String> getInputNames(String line) {
        String inputsString = line.substring( 
                line.indexOf(" ", StringUtils.ordinalIndexOf(line, "\"", 2) + 2) +2);
        
        inputsString = inputsString.substring(0, inputsString.length() -1);
        String[] inputsArray = inputsString.split(" \"");
        List<String> result = new ArrayList<>();
        
        for (String inputName : inputsArray) {
            result.add(
                    inputName.substring(0, inputName.length()-1));
        }
        
        return result;
    }

    private static TreeNode findRoot(String galileoFilePath, Map<String, TreeNode> mapNodes) throws IOException, MalformedGalileoFileException{
        try (BufferedReader br = new BufferedReader(new FileReader(galileoFilePath))) {
            //Find the first line
            String line;
            do{
                line = br.readLine();
                if(line == null)
                    throw new MalformedGalileoFileException("First line should specify 'toplevel'!");
                line = removeCommentIfPresents(line);
                if(skipLine(line))
                    line = null;
            }while(line == null);
            //Parse it
            if(!line.startsWith(TOPLEVEL_LABEL))
                throw new MalformedGalileoFileException("First line should specify 'toplevel'!");
            int startIndex = line.indexOf("\"");
            int stopIndex = line.lastIndexOf("\"");
            if(startIndex == -1 || stopIndex == -1 || startIndex == stopIndex)
                throw new MalformedGalileoFileException("Root element not specified correctly");
            
            TreeNode foundRoot = mapNodes.get(line.substring(startIndex + 1, stopIndex));
            if(foundRoot == null)
                throw new MalformedGalileoFileException("Root element not specified correctly");
            
            return foundRoot;
        } catch (IOException e) {
            throw e;
        }
    }
    
    private static TreeNode buildTreeNodeWithoutLinks(String line) throws MalformedGalileoFileException{
        String name = getName(line);
        NodeType type = getType(line);
        
        switch (type) {
            case and:
                return new AndPort(name);
            case or:
                return new OrPort(name);
            case pand:
                return new PriorityAndPort(name);
            case fdep:
                return new FunctionalDependencyPort(name);
            case fxdep:
                return new FailDependencyPort(name);
            case wsp:
                return new SparePort(name, SpareType.warm);
            case csp:
                return new SparePort(name, SpareType.cold);
            case hsp:
                return new SparePort(name, SpareType.hot);
            case xofy:
                Integer x = getXOFY_X(line);
                Integer y = getXOFY_Y(line);
                return new KOfNPort(name, x, y);
            case be:
                return buildBasicEvent(line);
            case repairbox_priority:
                return new RepairBox(name, RepairBox.Policy.priority);
            case repairbox_random:
                return new RepairBox(name, RepairBox.Policy.random);
            case repairbox_fcfs:
                return new RepairBox(name, RepairBox.Policy.fcfs);
            case repairbox_single:
                return new RepairBox(name, RepairBox.Policy.single);
            default:
                throw new UnsupportedOperationException("This type of port is not yet supported");
        }
    }
    
    private static BasicEvent buildBasicEvent(String line) throws MalformedGalileoFileException{
        if(line.indexOf(PROB_PARAMETER_LABEL) != -1)
            throw new UnsupportedOperationException("Discrete probability basic event are not supported since IOSA is a continuos time model!\n" + line);
        
        //Read common parameters
        String name = getName(line);
        double dorm = 0;
        String dormValue = getParameterValue(line, DORMANCY_PARAMETER_LABEL);
        if(dormValue != null)
            dorm = Double.parseDouble(dormValue);
        String repairPDFDescription = getParameterValue(line, EXTENSION_REPAIR_PDF);
        Distribution repairPDF = null;
        if(repairPDFDescription != null)
            //Note: in any case, repair distribution is represented as OtherDistribution, also if it is a supported type like exponential.
            //This is a simplification that can be done since the repair PDF is directly expressed in fig format
            repairPDF = new OtherDistribution(repairPDFDescription);
        String dormPDFDescription = getParameterValue(line, EXTENSION_DORMANCY_PDF);
        Distribution dormancyPDF = null;
        if(dormPDFDescription != null)
            //Note: in any case, dorm distribution is represented as OtherDistribution, also if it is a supported type like exponential.
            //This is a simplification that can be done since the dorm PDF is directly expressed in fig format
            dormancyPDF = new OtherDistribution(dormPDFDescription);
        
        //Switch based on the type of distribution
        if(line.indexOf(EXP_LAMBDA_PARAMETER_LABEL) != -1){//EXP case
            //A single distribution type should be specified
            if(line.indexOf(WEIBULL_RATE_PARAMETER_LABEL) != -1 || line.indexOf(LOGNORMAL_MEAN_PARAMETER_LABEL) != -1 || line.indexOf(WEIBULL_RATE_PARAMETER_LABEL) != -1 || line.indexOf(LOGNORMAL_MEAN_PARAMETER_LABEL) != -1 || line.indexOf(EXTENSION_FAIL_PDF) != -1)
                throw new MalformedGalileoFileException("Malformed basic event, specify only a distribution!\n" + line);
            
            double lambda = Double.parseDouble(getParameterValue(line, EXP_LAMBDA_PARAMETER_LABEL));
            return new BasicEvent(name, dorm, new Exponential(lambda), repairPDF, dormancyPDF);
            
        }else if(line.indexOf(WEIBULL_RATE_PARAMETER_LABEL) != -1 && line.indexOf(WEIBULL_SHAPE_PARAMETER_LABEL) != -1 ){//Weibull case
            //A single distribution type should be specified
            if(line.indexOf(EXP_LAMBDA_PARAMETER_LABEL) != -1|| line.indexOf(LOGNORMAL_MEAN_PARAMETER_LABEL) != -1 || line.indexOf(LOGNORMAL_SD_PARAMETER_LABEL) != -1 || line.indexOf(EXTENSION_FAIL_PDF) != -1)
                throw new MalformedGalileoFileException("Malformed basic event, specify only a distribution!\n" + line);
            
            double rate = Double.parseDouble(getParameterValue(line, WEIBULL_RATE_PARAMETER_LABEL));
            double shape = Double.parseDouble(getParameterValue(line, WEIBULL_SHAPE_PARAMETER_LABEL));
            return new BasicEvent(name, dorm, new Weibull(rate, shape), repairPDF, dormancyPDF);
            
        }else if(line.indexOf(LOGNORMAL_MEAN_PARAMETER_LABEL) != -1  && line.indexOf(LOGNORMAL_SD_PARAMETER_LABEL) != -1 ){//Log-normal case
            //A single distribution type should be specified
            if(line.indexOf(EXP_LAMBDA_PARAMETER_LABEL) != -1|| line.indexOf(WEIBULL_RATE_PARAMETER_LABEL) != -1 || line.indexOf(WEIBULL_SHAPE_PARAMETER_LABEL) != -1 || line.indexOf(EXTENSION_FAIL_PDF) != -1)
                throw new MalformedGalileoFileException("Malformed basic event, specify only a distribution!\n" + line);
            
            double mean = Double.parseDouble(getParameterValue(line, LOGNORMAL_MEAN_PARAMETER_LABEL));
            double sd = Double.parseDouble(getParameterValue(line, LOGNORMAL_SD_PARAMETER_LABEL));
            return new BasicEvent(name, dorm, new LogNormal(mean, sd), repairPDF, dormancyPDF);
        }else if(line.indexOf(EXTENSION_FAIL_PDF) != -1){//Other distribution case
            //A single distribution type should be specified
            if(line.indexOf(EXP_LAMBDA_PARAMETER_LABEL) != -1|| line.indexOf(WEIBULL_RATE_PARAMETER_LABEL) != -1 || line.indexOf(WEIBULL_SHAPE_PARAMETER_LABEL) != -1|| line.indexOf(LOGNORMAL_MEAN_PARAMETER_LABEL) != -1 || line.indexOf(LOGNORMAL_SD_PARAMETER_LABEL) != -1)
                throw new MalformedGalileoFileException("Malformed basic event, specify only a distribution!\n" + line);
            
            String description = getParameterValue(line, EXTENSION_FAIL_PDF);
            return new BasicEvent(name, dorm, new OtherDistribution(description), repairPDF, dormancyPDF);
        }else{//Error
            throw new MalformedGalileoFileException("Basic event without a valid distribution!\n" + line);
        }
       
    }
    
    private static int indexOfOrdered(String target, int beginIndex, String... args){
        for (String arg : args) {
            int pos = target.indexOf(arg, beginIndex);
            if(pos != -1)
                return pos;
        }
        return -1;
    }
    
    private static String getName(String line){
        String name = line.substring(
                line.indexOf("\"")+1, 
                StringUtils.ordinalIndexOf(line, "\"", 2));
        return name;
    }
    
    private static NodeType getType(String line) throws MalformedGalileoFileException{
        int typeStartIndex = StringUtils.ordinalIndexOf(line, "\"", 2) + 2;
        int typeStopIndex = line.indexOf(" ", StringUtils.ordinalIndexOf(line, "\"", 2) + 2);
        if(typeStopIndex == -1)
            typeStopIndex = line.indexOf(";", StringUtils.ordinalIndexOf(line, "\"", 2) + 2);
        if(typeStartIndex == -1 || typeStopIndex == -1)
            throw new MalformedGalileoFileException("Parsing error on line: " + line);
        
        String type = line.substring(typeStartIndex, typeStopIndex);
        
        //Special case 1: port xofy
        if(type.contains("of")){
            Integer x = getXOFY_X(line);
            Integer y = getXOFY_Y(line);
            
            if(x != null && y != null)
                return NodeType.xofy;
        }
        
        //Special case 2: basic event
        try{
            //valueOf() throw an exception if the string is not one of its element
            NodeType.valueOf(type);
        }catch(IllegalArgumentException e){
            return NodeType.be;
        }
        
        return NodeType.valueOf(type);
    }
    
    private static Integer getXOFY_X(String line) {
        String type = line.substring(
                StringUtils.ordinalIndexOf(line, "\"", 2) + 2, 
                line.indexOf(" ", StringUtils.ordinalIndexOf(line, "\"", 2) + 2));
        String before = type.substring(0, type.indexOf("of"));
        try{
            return Integer.parseInt(before);
        }catch(NumberFormatException e){
            return null;    
        }
    }

    private static Integer getXOFY_Y(String line) {
        String type = line.substring(
                StringUtils.ordinalIndexOf(line, "\"", 2) + 2, 
                line.indexOf(" ", StringUtils.ordinalIndexOf(line, "\"", 2) + 2));
        String after =  type.substring(type.indexOf("of") +2);
        try{
            return Integer.parseInt(after);
        }catch(NumberFormatException e){
            return null;    
        }
    }

    private static String getParameterValue(String line, String key){
        if(line.indexOf(key + "=") != -1)
            return line.substring(
                    line.indexOf(key + "=") + (key.length() +1),         
                    indexOfOrdered(line, line.indexOf(key + "="), " ", ";"));
        return null;
    }

}
