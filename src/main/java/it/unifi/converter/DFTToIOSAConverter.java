//==============================================================================
//
//  DFTToIOSAConverter.java
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


package it.unifi.converter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import it.unifi.converter.exceptions.ConversionException;
import it.unifi.converter.model.dft.DFT;
import it.unifi.converter.model.dft.FDEPRemoverVisitor;
import it.unifi.converter.model.dft.PANDSimplifierVisitor;
import it.unifi.converter.model.dft.TreeNode;
import it.unifi.converter.model.dft.event.BasicElement;
import it.unifi.converter.utils.MaxImportanceEvaluator;
import it.unifi.converter.visitors.CompositionalImportanceFunctionFIG;
import it.unifi.converter.visitors.ConverterVisitor;
import it.unifi.converter.visitors.TopEventPropertyVisitor;

/* This class allows to convert a DFT to IOSA-C in a the FIG file format.
* 
* FIG specification:
* http://dsg.famaf.unc.edu.ar/fig/iosa
*/
public class DFTToIOSAConverter {

    private static final String BASE_TEMPLATE_FOLDERS = "templates/";
    private static final double EPSILON_TIME_LIMIT_RATIO = 100;
    
//    private static final String FIG_CMD_FLAT_WITH_STOP_CONF = "fig <filePath> --flat --stop-conf .8 .4 --timeout 2m";
    private static final String FIG_CMD_FLAT = "fig <filePath> --flat --stop-time 5m";
//    private static final String FIG_CMD_RESTART_ES_WITH_STOP_CONF = "fig <filePath> --dft 0 --stop-conf .8 .4 --timeout 2m -e restart -t es";
//    private static final String FIG_CMD_RESTART_HYB_WITH_STOP_CONF = "fig <filePath> --dft 0 --stop-conf .8 .4 --timeout 2m -e restart -t hyb";
//    private static final String FIG_CMD_FIXEDEFFORT_ES_WITH_STOP_CONF = "fig <filePath> --dft 0 --stop-conf .8 .4 --timeout 2m -e sfe -t es";
//    private static final String FIG_CMD_FIXEDEFFORT_HYB_WITH_STOP_CONF = "fig <filePath> --dft 0 --stop-conf .8 .4 --timeout 2m -e sfe -t hyb";
    
    private static final String FIG_CMD_RESTART_ES = "fig <filePath> --dft 0 --stop-time 5m -e restart -t es";
    private static final String FIG_CMD_RESTART_HYB = "fig <filePath> --dft 0 --stop-time 5m -e restart -t hyb";
//    private static final String FIG_CMD_FIXEDEFFORT_ES = "fig <filePath> --dft 0 --stop-time 5m -e sfe -t es";
    private static final String FIG_CMD_FIXEDEFFORT_HYB = "fig <filePath> --dft 0 --stop-time 5m -e sfe -t hyb";


    public static void convertToIOSA(DFT dft, String targetFilePath, boolean addReliabilityProperty, boolean addStateReachability, boolean addAvailabilityProperty, Float timeLimit, boolean composedImportance, boolean normalizeComposedImportanceFunction) throws ConversionException{
        try {
            PrintWriter writer = new PrintWriter(targetFilePath, "UTF-8");
            convertToIOSA(dft, writer, addReliabilityProperty, addStateReachability, addAvailabilityProperty, timeLimit, composedImportance, normalizeComposedImportanceFunction);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new ConversionException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ConversionException(e.getMessage());
        }
    }
    
    public static void convertToIOSA(DFT dft, Writer writer, boolean addReliabilityProperty, boolean addStateReachability, boolean addAvailabilityProperty, Float timeLimit, boolean composedImportance, boolean normalizeComposedImportanceFunction) throws ConversionException{
            //1- Convert FDEP in OR gates
            FDEPRemoverVisitor fdepRemover = new FDEPRemoverVisitor(dft);
            DFT noFDEPDft = fdepRemover.getEquivalentTree();

            //2- Replace n-inputs PAND with cascade of 2-inputs PAND
            PANDSimplifierVisitor pandSimplifier = new PANDSimplifierVisitor(noFDEPDft);
            DFT newDft = pandSimplifier.getEquivalentTree();
            
            //3- Create structure starting visiting from root
            ConverterVisitor converterVisitor = new ConverterVisitor(writer, newDft);
            newDft.getRoot().accept(converterVisitor);
            
            //4- Continue creating structure, for each repair box 
            //(FXDEP are not required since are directly integrated in BE in step 3)
            //(FDEP are not required since are removed in step 1)
            newDft.getRepairBoxes().forEach(e -> e.accept(converterVisitor));
            
            //4- Add state reachability additional module if required
            if(addStateReachability)
                addStateReachabilityMonitor(newDft, writer, converterVisitor.getAssignedId());
            
            //5- Add properties if required
            Long rootAssignedId = converterVisitor.getAssignedId().get(newDft.getRoot());
            if(addReliabilityProperty || addAvailabilityProperty || addStateReachability)
                addProperties(newDft, writer, rootAssignedId, addReliabilityProperty, addStateReachability, addAvailabilityProperty, timeLimit);
            
            //6- Add composed importance function
            if(composedImportance)
                addComposedImportanceFunction(writer, newDft, converterVisitor, normalizeComposedImportanceFunction);
    }

    private static void addStateReachabilityMonitor(DFT newDft, Writer writer, Map<TreeNode, Long> assignedId) {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        
        Set<Long> BEIdList = getAllBEs(assignedId);
        
        
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/special/RegenMonitorTemplate.vm" );
        VelocityContext context = new VelocityContext();
        context.put("BEIdList", BEIdList);
        t.merge(context, writer);
        
    }

    private static Set<Long> getAllBEs(Map<TreeNode, Long> assignedId) {
        Set<Long> BEIdList = new LinkedHashSet<>();
        for (TreeNode node : assignedId.keySet()) {
            if(node instanceof BasicElement)
                BEIdList.add(assignedId.get(node));
        }
        return BEIdList;
    }

    private static void addComposedImportanceFunction(Writer writer, DFT dft, ConverterVisitor converterVisitor, boolean normalizeComposedImportanceFunction){
        
        CompositionalImportanceFunctionFIG formatter = new CompositionalImportanceFunctionFIG(converterVisitor.getAssignedId(), normalizeComposedImportanceFunction);
        
        String composedFunction = dft.getRoot().format(formatter);
        int maxImportance = MaxImportanceEvaluator.evaluate(composedFunction);
        String composedFunctionWithMinAndMax = composedFunction + ";0;" + maxImportance;
        
        try{
            //Append examples of FIG command using such function at the end of the file in a comment
            writer.write("\n// FIG command examples:");
            writer.write("\n// " + FIG_CMD_FLAT);
            writer.write("\n// " + FIG_CMD_RESTART_ES + " --acomp '" + composedFunctionWithMinAndMax + "'");
            writer.write("\n// " + FIG_CMD_RESTART_HYB + " --acomp '" + composedFunctionWithMinAndMax + "'");
//            writer.write("\n// " + FIG_CMD_FIXEDEFFORT_ES + " --acomp '" + composedFunctionWithMinAndMax + "'");
            writer.write("\n// " + FIG_CMD_FIXEDEFFORT_HYB + " --acomp '" + composedFunctionWithMinAndMax + "'");
            writer.write("\n// Compositional importance function for FIG tool:");
            writer.write("\n// " + composedFunctionWithMinAndMax);
        }catch(IOException e){
            new ConversionException(e.getMessage());
        }
    }

    private static void addProperties(DFT dft, Writer writer, Long rootAssignedId, boolean addReliabilityProperty, boolean addStateReachability, boolean addAvailabilityProperty, double timeLimit){
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();
        
        //Evaluate top event condition
        TopEventPropertyVisitor visitor = new TopEventPropertyVisitor(rootAssignedId);
        dft.getRoot().accept(visitor);
        String condition = visitor.getTopEventProperty();
        
        if(addReliabilityProperty){
            //Add timer for reliability
            Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/property/ReliabilityTimerTemplate.vm" );
            VelocityContext context = new VelocityContext();
            context.put("time_limit", new BigDecimal("" + timeLimit).toPlainString());
            double epsilon = timeLimit/EPSILON_TIME_LIMIT_RATIO;
            context.put("epsilon", new BigDecimal("" + epsilon).toPlainString());
            t.merge(context, writer);
        }
        
        //Begin properties
        Template t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/property/BeginPropertiesTemplate.vm" );
        t.merge(new VelocityContext(), writer);
        
        //Reliability property
        if(addReliabilityProperty){
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/property/ReliabilityPropertyTemplate.vm" );
            VelocityContext context = new VelocityContext();
            context.put("condition_fail_root", condition);
            t.merge(context, writer);
        }
        
        //State reachability property
        if(addStateReachability){
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/property/StateReachabilityPropertyTemplate.vm" );
            VelocityContext context = new VelocityContext();
            context.put("condition_fail_root", condition);
            t.merge(context, writer);
        }
        
        //Availability property
        if(addAvailabilityProperty){
            t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/property/AvailabilityPropertyTemplate.vm" );
            VelocityContext context = new VelocityContext();
            context.put("condition_fail_root", condition);
            t.merge(context, writer);
        }
        
        //End properties
        t = ve.getTemplate( BASE_TEMPLATE_FOLDERS + "iosa/property/EndPropertiesTemplate.vm" );
        t.merge(new VelocityContext(), writer);
    }
    
}
