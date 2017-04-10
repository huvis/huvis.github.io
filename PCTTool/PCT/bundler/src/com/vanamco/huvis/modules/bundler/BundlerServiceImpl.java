 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.bundler;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.api.bundlerapi.scene.Camera;
import com.vanamco.huvis.api.bundlerapi.scene.Scene;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *  This is a concrete implementation of {@link com.vanamco.huvis.api.bundlerapi.BundlerService}.
 * @author ybrise
 */
public class BundlerServiceImpl extends BundlerService {

    /**
     * Constructor
     * @param bundlerProperties The properties to be conferred to the current instance.
     */
    public BundlerServiceImpl(BundlerProperties bundlerProperties) {
        super(bundlerProperties, null);
    }

    @Override
    public void bundle(FileObject[] scenePictures, FileObject workingDir) {
        bundle(scenePictures, workingDir, true);
        
    }

    @Override
    public Scene getScene(FileObject bundlerWorkingDir) {

        FileObject bundlerOutputDir = bundlerWorkingDir.getFileObject(BundlerProperties.OUTPUT_DIRECTORY_NAME);
        if (bundlerOutputDir != null && scene == null) {
            try {
                scene = new Scene(bundlerOutputDir, Color.yellow, Color.pink, bundlerProperties.getImageProportion(), bundlerProperties.getOpeningAngle(), bundlerProperties.getFeatureSize());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return scene;
    }

    /**
     *
     * Returns an instance of {@link Preferences}.
     * 
     * @return Preferences
     */
    public static Preferences getPreferences() {
        return Preferences.userNodeForPackage(BundlerServiceImpl.class);
    }

    @Override
    public void bundle(FileObject[] scenePictures, FileObject workingDir, boolean clean) {
        BundlerExternal bundlerExternal = new BundlerExternal(this, workingDir, clean);
        try {
            bundlerExternal.bundle(scenePictures);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void generatePovrayScript(FileObject bundleOutputDir, FileObject workingDir, String preamble) {

        StringBuffer povScripts[] = new StringBuffer[0];
        String[] titles = new String[0];
        try {
            povScripts = Bundle2Pov.generate(bundleOutputDir, transformation, Color.yellow, Color.pink, bundlerProperties.getImageProportion(), bundlerProperties.getOpeningAngle(), bundlerProperties.getFeatureSize(), this);
            Camera[] cams = scene.getCameras();
            titles = new String[povScripts.length];
            for (int i = 0; i < povScripts.length; ++i) {
                if (i < cams.length) {
                    String tmp = cams[i].getFilename();
                    titles[i] = tmp.substring(0,tmp.lastIndexOf('.'));
                } else {
                    titles[i] = "sky" + String.valueOf(i - povScripts.length + 1);
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        // write out files
        for (int i = 0; i < povScripts.length; ++i) {
            File povrayFile = new File(workingDir.getPath() + PCTSettings.FILESEPARATOR + BundlerProperties.POVRAY_DIRECTORY_NAME + PCTSettings.FILESEPARATOR + titles[i] + ".pov");
            if (povrayFile.exists()) {
                povrayFile.delete();
            }
            try {
                povrayFile.createNewFile();
                PrintWriter out = new PrintWriter(new FileWriter(povrayFile));
                //out.println(pov.toString());
                out.println(preamble);
                out.println();
                out.println(povScripts[i]);
                out.flush();
                out.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
