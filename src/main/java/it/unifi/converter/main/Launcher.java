//==============================================================================
//
//  Launcher.java
//
//  Copyright 2018-
//  Authors:
//  - Marco Biagi <marcobiagiing@gmail.com> (Universita di Firenze)
//  - Carlos E. Budde <c.e.budde@utwente.nl> (Universiteit Twente)
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import it.unifi.converter.DFTToIOSAConverter;
import it.unifi.converter.model.dft.DFT;

/**
 * Given the path of a DFT specified in the Galileo format,
 * convert it to an IOSA specified in the FIG file format.
 * 
 * @author Marco Biagi
 */
public class Launcher {
    private final static HelpFormatter formatter = new HelpFormatter();
    private final static CommandLineParser parser = new DefaultParser();
    private final static Options options = definitionCLI();
    private final static String HELP_CMD_SYNTAX = "java -jar THIS_JAR_FILE [options*] INPUT_PATH OUTPUT_PATH";
    private final static String HELP_HEADER = "Options:";
    private final static String HELP_FOOTER = "Example usage: java -jar THIS_JAR_FILE -r=7.35 in.dft out.sa";
    

    public static void main(String[] args){
        //NOTE: UNCOMMENT ONLY FOR TESTING PURPOSE! COMMENT ON PRODUCTION
        //args = testArgs();
        
        //Parse CLI arguments
        CommandLine cmd;
        Float timeLimit = null;
        Boolean normalizeComposedImportanceFunction = false;
        try {
            cmd = parser.parse(options, args);
            if(cmd.getArgList().size() != 2)
                throw new ParseException("Please specify an input Galileo file and an output file path");
            if(cmd.hasOption('r')){//Ugly way to check if 'r' value is a Float... Perhaps Apache common-cli is not the best choice
                try{
                    timeLimit = Float.parseFloat(cmd.getOptionValue('r'));
                }catch(NumberFormatException e){
                    throw new ParseException("Reliability argument should be a float");
                }
            }
            if(cmd.hasOption('i')){
                if(!cmd.getOptionValue('i').equals("true") && !cmd.getOptionValue('i').equals("false"))
                    throw new ParseException("Importance argument should be a boolean");
                normalizeComposedImportanceFunction = Boolean.parseBoolean(cmd.getOptionValue('i'));
            }
                
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(HELP_CMD_SYNTAX, HELP_HEADER, options, HELP_FOOTER);
            System.exit(1);
            return;
        }
        
        String inputFilePath = cmd.getArgList().get(0);
        String outputFilePath = cmd.getArgList().get(1);
        boolean addReliabilityProperty = cmd.hasOption('r');
        boolean addStateReachability = cmd.hasOption('s');
        boolean addAvailabilityProperty = cmd.hasOption('a');
        boolean verbose = cmd.hasOption('v');
        boolean composedImportance = cmd.hasOption('i');
        
        //Execute conversion
        try{
            convert(inputFilePath, outputFilePath, addReliabilityProperty, addStateReachability, addAvailabilityProperty, timeLimit, composedImportance, normalizeComposedImportanceFunction);
        }catch(Exception e){
            printError(e, verbose);
        }
    }
    
    static void convert(String inputFilePath, String outputFilePath, boolean addReliabilityProperty,  boolean addStateReachability, boolean addAvailabilityProperty, Float timeLimit, boolean composedImportance, boolean normalizeComposedImportanceFunction) throws Exception{
        System.out.println("Parsing Galileo file");
        DFT dft = DFT.fromFile(inputFilePath);
        
        System.out.println("Converting DFT to IOSA");
        DFTToIOSAConverter.convertToIOSA(dft, outputFilePath, addReliabilityProperty, addStateReachability, addAvailabilityProperty, timeLimit, composedImportance, normalizeComposedImportanceFunction);

        System.out.println("Conversion completed!");
    }
    
    private static String[] testArgs(){
        String[] args = new String[7];
        args[0] = "/home/marco/Scrivania/and_8_005.dft";
//      args[0] = "src/main/resources/models/customized/hcas/cas_with_repairs.dft";
//      args[0] = "src/main/resources/models/examples/testWithExtensions_2.dft";
//      args[0] = "src/main/resources/models/benchmarks/normalized/hecs/hecs_1_1_0_np.dft";
//        
        args[1] = "/home/marco/Scrivania/and_8_005.sa";
        args[2] = "-r=10";
        args[3] = "-v";
        args[4] = "-s";
        args[5] = "-i=true";
        args[6] = "-a";
        return args;
    }
    
    private static Options definitionCLI(){
        Options options = new Options();

        Option verbose = new Option("v", "verbose", false, "enable verbose mode");
        verbose.setRequired(false);
        options.addOption(verbose);

        Option reliability = new Option("r", "reliability", true, "if specified append a reliability property to generated file with specified <arg> as time value");
        reliability.setRequired(false);
        reliability.setType(Number.class);
        options.addOption(reliability);
        
        Option availability = new Option("a", "availability", false, "if specified append an v property to generated file");
        reliability.setRequired(false);
        options.addOption(availability);
        
        Option stateReachability = new Option("s", "state-reachability", false, "if specified append a module and a property to generated file");
        stateReachability.setRequired(false);
        options.addOption(stateReachability);
        
        Option figComposedImportanceFunction = new Option("i", "compImportance", true, 
                "generate a \"compositional\" importance function for the FIG tool based on the fault tree structure. "
                + "The formula will be appended in a comment at the end of the file."
                + "Boolean parameter is mandatory, if true the function will be normalized at each level so as to that gate inputs has the same importance");
        figComposedImportanceFunction.setRequired(false);
        reliability.setType(Boolean.class);
        options.addOption(figComposedImportanceFunction);
        
        return options;
    }
    
    private static void printError(Exception e, boolean verbose){
        System.out.println("ERROR: " + e.getMessage());
        if(verbose){
            System.out.println("Stack trace:");
            e.printStackTrace();
        }
    }
}
