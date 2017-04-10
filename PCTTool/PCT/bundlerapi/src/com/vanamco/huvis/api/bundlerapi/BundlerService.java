/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.api.bundlerapi;

import com.vanamco.huvis.api.bundlerapi.scene.Scene;
import com.vanamco.huvis.modules.toolbox.geometry.Transformation;
import com.vanamco.huvis.modules.toolbox.geometry.TransformationJAG3D;
import org.openide.filesystems.FileObject;

/**
 * This class defines the functionality for accessing the Bundler process. It
 * represents the interface between the actual Bundler implementation and the PCT
 * application.
 * @author ybrise
 */
public abstract class BundlerService {

    protected BundlerProperties bundlerProperties;
    protected Scene scene;
    protected Transformation transformation;

    /**
     * Constructor.
     * @param bundlerProperties
     * @param scene
     */
    public BundlerService(BundlerProperties bundlerProperties, Scene scene) {
        this.bundlerProperties = (bundlerProperties == null ? new BundlerProperties(null, null) : bundlerProperties);
        this.scene = scene;
        this.transformation = new TransformationJAG3D();
    }
    /**
     *
     */
    public static final String PROJECT_BUNDLER_KEY_PREFIX = "renderer.";

    /**
     * Use this method to run the Bundler process on a set of pictures.
     * @param scenePictures The pictures to be bundled.
     * @param workingDir The working directory of the process, where all files will
     *                   be deposited.
     */
    public abstract void bundle(FileObject[] scenePictures, FileObject workingDir);

    /**
     *
     * Same as {@link #bundle(org.openide.filesystems.FileObject[], org.openide.filesystems.FileObject) bundle(pics, dir)},
     * except that there is a flag to indicate whether the working directory should
     * be cleaned before the Bundler process is run.
     * @param scenePictures The pictures to be bundled.
     * @param workingDir The working directory of the process, where all files will
     *                   be deposited.
     * @param clean      If true, the workingDir is cleaned (emptied) before
     *                   the Bundler process is run.
     */
    public abstract void bundle(FileObject[] scenePictures, FileObject workingDir, boolean clean);

    /**
     *
     * @param scenePicture
     * @param workingDir
     * @param preamble
     */
    public abstract void generatePovrayScript(FileObject scenePicture, FileObject workingDir, String preamble);

    /**
     *
     * @return
     */
    public BundlerProperties getProperties() {
        return bundlerProperties;
    }

    /**
     *
     * @param bundlerWorkingDir
     * @return
     */
    public Scene getScene(FileObject bundlerWorkingDir) {
        return scene;
    }

    /**
     *
     * @param properties
     */
    public void setProperties(BundlerProperties properties) {
        this.bundlerProperties = properties;
        this.bundlerProperties.consolidate();
    }

    /**
     *
     * @param scene
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     *
     * @param transformation
     */
    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }
    
    /**
     * 
     */
    public void removeTransformation() {
        this.transformation = new TransformationJAG3D();
    }

    /**
     *
     */
    public void reset() {
        setScene(null);
        removeTransformation();
    }
}    
