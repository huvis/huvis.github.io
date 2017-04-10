/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.projectRelated;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 *
 * @author ybrise
 */
public class PCTSettings {

    /**
     * Indicates the name of the bundler directory inside the project directory.
     */
    public static final String DIR_BUNDLER = "bundler";
    /**
     * Indicates the name of the Povray directory inside the project directory.
     */
    public static final String DIR_POVRAY = "povray";
    /**
     * Indicates the name of the input directory that the user should use
     * to deposit all input files.
     */
    public static final String DIR_INPUT = "input";
    /**
     * Indicates the name of the output directory, where the final renderings
     * and output files are deposited by PCT software.
     */
    public static final String DIR_OUTPUT = "output";
    /**
     * Indicates the name of the administrative directory inside the project
     * directory. This is used to store properties and settings of the project.
     * This directory must not be deleted, or else the project will not be
     * recognized correctly any more.
     */
    public static final String DIR_PROJECT = "pctproject";
    /**
     * The name of the main property file.
     */
    public static final String PROPFILE_PROJECT = "project.properties";
    /**
     * The name of the property file that stores renderer properties.
     */
    public static final String PROPFILE_RENDERER = "renderer.properties";
    /**
     * The name of the property file that stores bundler properties.
     */
    public static final String PROPFILE_BUNDLER = "bundler.properties";
    /**
     * The three letter extension of Povray main files to render.
     */
    public static final String EXTENSION_POVRAY_TYPE = "pov";
    /**
     * The three letter extension of the Povray include files.
     */
    public static final String EXTENSION_POVRAY_INCLUDE_TYPE = "inc";
    /**
     * The three letter extension of the main picture input file type.
     */
    public static final String EXTENSION_INPUT_TYPE = "jpg";
    /**
     * The three letter extension of the main picture output file type.
     */
    public static final String EXTENSION_OUTPUT_TYPE = "jpg";
    /**
     * The three letter extension of the rendered files that Povray produces.
     */
    public static final String EXTENSION_RENDER_TYPE = "png";
    /**
     * The exact name of the transformation file as provided be JAG3D. The name
     * cannot be different, or else the transformation will not be correctly
     * applied.
     */
    public static final String TRANSFORMATION_FILE_NAME = "transformation.jag3d";
    /**
     * The system dependent newline string.
     */
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String FILESEPARATOR = System.getProperty("file.separator");
    public static final String TABULATOR = "\t";
    
    public static final String OUTPUT_REGISTER_WORKFLOW = "Workflow";
    public static final String OUTPUT_REGISTER_BUNDLER = "Bundler";
    public static final String OUTPUT_REGISTER_POVRAY = "Povray";
    public static final String OUTPUT_REGISTER_TRANSFORMATION = "Transformation";
    public static final String OUTPUT_SEPARATOR = "****************************************************************";
    
    public static String timestamp() {
     SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     return timestampFormat.format(new Timestamp(System.currentTimeMillis()));
    }
    
}
