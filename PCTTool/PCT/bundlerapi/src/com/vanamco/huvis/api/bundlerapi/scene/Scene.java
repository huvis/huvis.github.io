/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.api.bundlerapi.scene;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.modules.toolbox.geometry.Sphere;
import com.vanamco.huvis.modules.toolbox.geometry.Transformation;
import com.vanamco.huvis.modules.toolbox.povRelated.PovrayParser;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 * @author fmueller
 */
public class Scene implements Serializable {

    private int nCameras = 0;
    private Camera[] cameras = new Camera[0];
    private int nPoints = 0;
    private Sphere[] features = new Sphere[0];
    private String camColorString = PovrayParser.toPov(Color.RED);
    private String pointColorString = PovrayParser.toPov(Color.BLUE);
    private float proportion = 1.0f;
    private float angle = 10.0f;
    private float featureSize = 0.1f;

    public Scene(FileObject bundlerOutputDir, Color camColor, Color pointColor,
            float proportion, float angle, float featureSize) throws Exception {

        //Get an InputOutput to write to the output window
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        InputOutput io = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_BUNDLER, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Scene] trying to parse Bundler output");

        this.camColorString = PovrayParser.toPov(camColor);
        this.pointColorString = PovrayParser.toPov(pointColor);
        this.proportion = proportion;
        this.angle = angle;
        this.featureSize = featureSize;

        // retreive bundler output file
        FileObject bundlerOutputFile = bundlerOutputDir.getFileObject(BundlerProperties.BUNDLE_FILE_NAME);

        if (bundlerOutputFile != null) {
            BufferedReader in = new BufferedReader(new FileReader(new File(bundlerOutputFile.getPath())));
            io.getOut().println("reading output file " + bundlerOutputFile.getName());

            String header = in.readLine().trim();

            if (!header.equals("# Bundle file v0.3")) {
                throw new Exception("Invalid file: " + bundlerOutputFile.getName());
            }

            String[] atoms = in.readLine().split(" ");
            nCameras = Integer.parseInt(atoms[0]);
            nPoints = Integer.parseInt(atoms[1]);

            String timestamp = PCTSettings.timestamp();
            io.getOut().print(timestamp);
            io.getOut().println(" [Scene] detected " + nCameras + " cameras and " + nPoints + " points");
            io_wf.getOut().print(timestamp);
            io_wf.getOut().println(" [Scene] detected " + nCameras + " cameras and " + nPoints + " points");


            cameras = new Camera[nCameras];

            // get names the cameras correspond to
            String[] cameraNames = new String[nCameras];
            // null initialize
            for (int i = 0; i < nCameras; ++i) {
                cameraNames[i] = null;
            }
            // try to get the real names
            BufferedReader listJpgs = new BufferedReader(new FileReader(FileUtil.toFile(bundlerOutputDir.getFileObject(BundlerProperties.OUTPUT_LIST_JPGS))));
            if (listJpgs != null) {
                for (int i = 0; i < nCameras; ++i) {
                    cameraNames[i] = listJpgs.readLine();
                }
            }


            for (int i = 0; i < nCameras; i++) {
                String focalString = in.readLine();
                String rot1String = in.readLine();
                String rot2String = in.readLine();
                String rot3String = in.readLine();
                String transString = in.readLine();
                cameras[i] = new Camera(focalString, rot1String, rot2String, rot3String, transString, this.proportion, this.angle, cameraNames[i]);
            }

            features = new Sphere[nPoints];

            for (int i = 0; i < nPoints; i++) {

                atoms = in.readLine().split(" ");
                double x = Double.parseDouble(atoms[0]);
                double y = Double.parseDouble(atoms[1]);
                double z = Double.parseDouble(atoms[2]);

                atoms = in.readLine().split(" "); // discard color line


                //double red = (double) Integer.parseInt(atoms[0]) / 255.0;
                //double green = (double) Integer.parseInt(atoms[1]) / 255.0;
                //double blue = (double) Integer.parseInt(atoms[2]) / 255.0;

                int red = Integer.parseInt(atoms[0]);
                int green = Integer.parseInt(atoms[1]);
                int blue = Integer.parseInt(atoms[2]);

                features[i] = new Sphere(x, y, z, featureSize, new Color(red, green, blue));
                //features[i] = new Sphere(x, y, z, 0.1, new Color(red, green, blue));
                //features[i] = new Sphere(x, y, z, 0.015, new Color(red, green, blue));

                //features[i] = new Sphere(x, y, z, 0.05, "rgb <" + red + "," + green + "," + blue + ">");

                in.readLine(); // discard image list line
            }

            io_wf.select();
            io_wf.getOut().print(PCTSettings.timestamp());
            io_wf.getOut().println(" [Scene] scene successfully loaded");
        } else {
            io_wf.getOut().print(PCTSettings.timestamp());
            io_wf.getOut().println(" [Scene] output file " + BundlerProperties.BUNDLE_FILE_NAME + " not found");
        }

    }

    /*
     * SceneFromBundle(String bundle1out, String string, String string0, String
     * string1, String string2) { throw new
     * UnsupportedOperationException("Not yet implemented"); }
     */
    public Camera[] getCameras() {
        return cameras;
    }

    public Sphere[] getPoints() {
        return features;
    }

    public void transform(Transformation transformation) {
        for (Camera camera : cameras) {
            camera.transform(transformation);
        }
        for (Sphere feature : features) {
            feature.transform(transformation);
        }
    }

    /*
     * 
     * Utility functions
     */
    public String toPov() {
        StringBuffer retVal = new StringBuffer();
        for (int i = 0; i < cameras.length; i++) {
            retVal.append(cameras[i].toPovrayObject());
        }
        for (int i = 0; i < features.length; i++) {
            retVal.append(features[i].toPov());
        }
        return retVal.toString();
    }
}
