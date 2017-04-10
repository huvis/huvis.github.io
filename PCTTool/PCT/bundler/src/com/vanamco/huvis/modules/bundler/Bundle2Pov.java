/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.bundler;

import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.api.bundlerapi.scene.Camera;
import com.vanamco.huvis.api.bundlerapi.scene.Scene;
import com.vanamco.huvis.modules.toolbox.geometry.Sphere;
import com.vanamco.huvis.modules.toolbox.geometry.Transformation;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.Color;
import javax.vecmath.Point3d;
import org.openide.filesystems.FileObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * Transforms the Bundler output to pov-files that can be rendered.
 * 
 * @author ybrise
 * @author fmueller
 */
public class Bundle2Pov {

    private static String light_source_prefix = "light_source {\n\t< ";
    private static String light_source_postfix = ">\n\trgb <1.000000, 0.999994, 1.000000>\n}\n";
    private static Point3d light_source_pos_a = new Point3d(0, 100, 0);
    private static Point3d light_source_pos_b = new Point3d(0, 100, 100);
    private static Point3d light_source_pos_c = new Point3d(100, 100, 0);

    /**
     *
     * Call this method to retrieve a StringBuffer that contains all the pov
     * scripts corresponding to the cameras in a scene. It is assumed that
     * the Bundler process was successfully executed before calling this
     * method.
     * 
     * @param bundlerOutputDir The output directory of the bundler process
     * @param transformation A transformation that is applied to all the points
     *                       in the scene.
     * @param camColor      The color that is used for rendering cameras.
     * @param pointColor    The color used for displaying detected features.
     * @param proportion    The image proportion (height/width).
     * @param angle         The opening angle of the camera.
     * @param featureSize   The size used for rendering detected features.
     * @param bundlerService    A reference to the projects BundlerService.
     * @return  An array of StringBuffer containing the pov scripts corresponding
     *          to the scene as seen from the camera perspectives.
     * @throws Exception
     */
    public static StringBuffer[] generate(FileObject bundlerOutputDir, Transformation transformation, Color camColor, Color pointColor, float proportion, float angle, float featureSize, BundlerService bundlerService) throws Exception {

        InputOutput io = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_TRANSFORMATION, false);
        io.select();
        io.getOut().println("The following transformation is loaded:");
        transformation.print(io.getOut());


        Scene scene = new Scene(bundlerOutputDir, camColor, pointColor, proportion, angle, featureSize);
        bundlerService.setScene(scene);

        Camera[] cams = scene.getCameras();
        Sphere[] points = scene.getPoints();

        StringBuffer ret[] = new StringBuffer[cams.length + 1];

        // apply the transformation to each camera
        for (int i = 0; i < cams.length; ++i) {
            cams[i].transform(transformation);
        }

        // apply the transformation to each feature     point
        for (int i = 0; i < points.length; ++i) {
            points[i].transform(transformation);
        }

        for (int i = 0; i < cams.length + 1; ++i) {

            ret[i] = new StringBuffer();

            //StringBuffer pov = new StringBuffer();
            ret[i].append("// Light sources\n//\n//\n");
            transformation.transform(light_source_pos_a);
            transformation.transform(light_source_pos_b);
            transformation.transform(light_source_pos_c);
            ret[i].append(light_source_prefix + light_source_pos_a.x + "," + light_source_pos_a.y + "," + light_source_pos_a.z + light_source_postfix);
            ret[i].append(light_source_prefix + light_source_pos_b.x + "," + light_source_pos_b.y + "," + light_source_pos_b.z + light_source_postfix);
            ret[i].append(light_source_prefix + light_source_pos_c.x + "," + light_source_pos_c.y + "," + light_source_pos_c.z + light_source_postfix);


            ret[i].append("//CAMERA\n//\n//\n");
            if (i < cams.length) {
                ret[i].append(cams[i].toPovrayCamera());
            } else if (i == cams.length) {
                // "sky camera"
                ret[i].append("// sky camera\n");
                ret[i].append("camera {\n\tperspective\n\tlocation < -2, 4, 0>\n\tright x * -1\n\tup y * 3/4\n\tangle 60\n\tlook_at < 0.0, 0.0, 0.0>\n}\n");
            }

            // camera
            ret[i].append("// Cams as Points\n//\n//\n");
            if (i == cams.length) {
                for (int j = 0; j < cams.length; j++) {
                    if (j != i) {
                        ret[i].append(cams[j].toPovrayObject());
                    }
                }
            }

            // points
            ret[i].append("\n\n// POINTS\n//\n//\n");
            for (int j = 0; j < points.length; j++) {
                ret[i].append(points[j].toPov());
            }

            int fileCount = 1000 + i;
        }
        return ret;
    }
}
