/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.api.bundlerapi.scene;

import com.vanamco.huvis.modules.toolbox.geometry.Transformation;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.util.Properties;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ybrise
 * @author fmueller
 */
public class Camera extends Properties {

    // BenQ C740i
    //public static final double PROPORTION = 0.75;
    //public static final double ANGLE = 55.5;
    // Canon PowerShot A10
    // public static final double PROPORTION = 0.75;
    // public static final double ANGLE = 50.0;
    // Apple iPhone 3G
    // public static final double PROPORTION = 0.75;
    // public static final double ANGLE = 70.0;
    private Point3d location;
    private Matrix3d rotation;
    private double focalLength;
    private double proportion;
    private double angle;
    private double[] distortionCoeffs = new double[2];
    //private double[] invRotVec = new double[3];
    private String color;
    private static double radius = 0.025;
    private int id = 0;
    private Point3d measuredPosition;
    private String fileFullPath = null;

    /**
     * Create a camera
     *
     * @param location
     * @param rotation
     * @param focalLength
     * @param coeff1
     * @param coeff2
     * @param color
     * @param proportion
     * @param angle
     */
    public Camera(Point3d location, Matrix3d rotation, double focalLength,
            double coeff1, double coeff2, String color, double proportion,
            double angle, String filename) {

        initCamera(location, rotation, focalLength, coeff1, coeff2,
                color, proportion, angle, filename);
    }

    /**
     *
     * @param focalString
     * @param rot1String
     * @param rot2String
     * @param rot3String
     * @param transString
     * @param proportion
     * @param angle
     * @param filename
     */
    public Camera(String focalString, String rot1String, String rot2String, String rot3String, String transString, double proportion, double angle, String filename) {
        double focalLength_loc = 0.0;
        double coeff1_loc = 0.0;
        double coeff2_loc = 0.0;
        double[] rotation_loc = new double[9];
        double[] translation_loc = new double[3];

        String[] atoms = focalString.split(" ");
        focalLength_loc = Double.parseDouble(atoms[0]);
        coeff1_loc = Double.parseDouble(atoms[1]);
        coeff2_loc = Double.parseDouble(atoms[2]);

        atoms = rot1String.split(" ");
        rotation_loc[0] = Double.parseDouble(atoms[0]);
        rotation_loc[1] = Double.parseDouble(atoms[1]);
        rotation_loc[2] = Double.parseDouble(atoms[2]);

        atoms = rot2String.split(" ");
        rotation_loc[3] = Double.parseDouble(atoms[0]);
        rotation_loc[4] = Double.parseDouble(atoms[1]);
        rotation_loc[5] = Double.parseDouble(atoms[2]);

        atoms = rot3String.split(" ");
        rotation_loc[6] = Double.parseDouble(atoms[0]);
        rotation_loc[7] = Double.parseDouble(atoms[1]);
        rotation_loc[8] = Double.parseDouble(atoms[2]);

        atoms = transString.split(" ");
        translation_loc[0] = Double.parseDouble(atoms[0]);
        translation_loc[1] = Double.parseDouble(atoms[1]);
        translation_loc[2] = Double.parseDouble(atoms[2]);

        Matrix3d locationMatrix = new Matrix3d(rotation_loc);
        Point3d locationVector = new Point3d(translation_loc);
        locationMatrix.transpose();
        locationMatrix.negate();
        locationMatrix.transform(locationVector);

        //Matrix.matrixVector(Matrix.matrixScalar(Matrix.transposeMatrix(rotation), -1), translation)

        initCamera(locationVector, new Matrix3d(rotation_loc), focalLength_loc, coeff1_loc, coeff2_loc, "rgb <1,0,0>", proportion, angle, filename);
    }

    // get methods
    /**
     *
     * @return
     */
    public double getAngle() {
        return angle;
    }

    /**
     *
     * @return
     */
    public Point3d getLocation() {
        return location;
    }

    /**
     *
     * @return
     */
    public Matrix3d getRotation() {
        return rotation;
    }

    /**
     *
     * @return
     */
    public double getFocalLength() {
        return focalLength;
    }

    /**
     *
     * @return
     */
    public double[] getCoefficients() {
        return distortionCoeffs;
    }

    /**
     *
     * @return
     */
    public double getProportion() {
        return proportion;
    }

    /**
     *
     * @return
     */
    public Point3d getMeasuredPosition() {
        return measuredPosition;
    }

    /**
     *
     * @return
     */
    public String getFileFullPath () {
        return fileFullPath;
    }
    
    /**
     *
     * @return
     */
    public String getFilename() {
        
        return fileFullPath.substring(fileFullPath.lastIndexOf(PCTSettings.FILESEPARATOR) + 1);
    }

    /**
     *
     * @param measuredPosition
     */
    public void setMeasuredPosition(Point3d measuredPosition) {
        this.measuredPosition = measuredPosition;
    }

    private void initCamera(Point3d location, Matrix3d rotation, double focalLength,
            double coeff1, double coeff2, String color, double proportion,
            double angle, String filename) {
        this.location = location;
        this.rotation = rotation;
        this.color = color;
        this.focalLength = focalLength;
        this.proportion = proportion;
        // TODO 666: remove the following hack, i.e. put in focalLength in mm an not in px dimension,
        // that way we won't need to know the image dimension in Camera (which we shouldn't, because
        // only the proportion is relevant)
        //this.angle = angle;
        this.angle = 2 * Math.toDegrees(Math.atan(1024.0 / (focalLength * 2.0)));
        this.measuredPosition = new Point3d();
        distortionCoeffs[0] = coeff1;
        distortionCoeffs[1] = coeff2;

        this.fileFullPath = filename;
        //invRotVec[0] = rotation[2][0] * -1;
        //invRotVec[1] = rotation[2][1] * -1;
        //invRotVec[2] = rotation[2][2] * -1;
    }

    /**
     *
     * @param transformation
     */
    public void transform(Transformation transformation) {
        transformation.transform(location);
        transformation.transform_rotation(rotation);
        //transformation.transform_translation(location);
        //transformation.transform_camera(rotation, location);
    }

    /**
     * Convert camera to sphere object in povray
     *
     * @return String
     */
    public String toPovrayObject() {
        return "sphere { \n <" + location.x + ", " + location.y + ", "
                + location.z + ">, " + radius + "\ntexture{pigment{" + color
                + "}}\n}\n";
    }

    /**
     * Convert to camera object in povray
     *
     * @return Sting
     */
    public String toPovrayCamera() {

        StringBuilder retVal = new StringBuilder();

        if (fileFullPath != null) {
            retVal.append("// corresponds to " + fileFullPath + "\n");
        }
        retVal.append("camera{\n");
        retVal.append("\tperspective\n");
        retVal.append("\tdirection <0,0,-1>\n");
        retVal.append("\tright x*1\n");
        retVal.append("\tup y*" + this.proportion + "\n");
        retVal.append("\tangle " + this.angle + "\n");
        retVal.append("matrix <	" + rotation.m00 + ", " + rotation.m01 + ", " + rotation.m02 + ",\n");
        retVal.append("			" + rotation.m10 + ", " + rotation.m11 + ", " + rotation.m12 + ",\n");
        retVal.append("			" + rotation.m20 + ", " + rotation.m21 + ", " + rotation.m22 + ",\n");
        retVal.append("			" + location.x + ", " + location.y + ", " + location.z + ">\n");
        retVal.append("}\n");

        return retVal.toString();
    }

    /**
     *
     * @return
     */
    public final Node getNodeDelegate() {
        return new CameraNode(this);

    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     */
    public static class CameraNode extends AbstractNode {

        private Camera camera;

        /**
         *
         * @param camera
         */
        public CameraNode(Camera camera) {
            super(Children.LEAF, Lookups.singleton(camera));
            setDisplayName("Cam " + camera.getId() + " (" + camera.getFilename() + ")");
            setIconBaseWithExtension("com/vanamco/huvis/api/bundlerapi/resources/camera_icon.gif");
            this.camera = camera;
        }

        /**
         *
         * @return
         */
        @Override
        protected Sheet createSheet() {

            Sheet sheet = Sheet.createDefault();
            Sheet.Set set = Sheet.createPropertiesSet();

            try {
                //Property camColorProp = new PropertySupport.Reflection(settings, Color.class, "getCamColor", "setCamColor");
                Property<Double> camProportion = new PropertySupport.Reflection<Double>(camera, Double.class, "getProportion", null);
                Property<Double> camFocalLength = new PropertySupport.Reflection<Double>(camera, Double.class, "getFocalLength", null);
                Property<Double> camAngle = new PropertySupport.Reflection<Double>(camera, Double.class, "getAngle", null);
                Property<Point3d> camMeasuredPosition = new PropertySupport.Reflection<Point3d>(camera, Point3d.class, "getMeasuredPosition", "setMeasuredPosition");

                camProportion.setName("image proportion");
                set.put(camProportion);
                camFocalLength.setName("focal length [px]");
                set.put(camFocalLength);
                camAngle.setName("opening angle [°]");
                set.put(camAngle);
                camMeasuredPosition.setName("measured position");
                set.put(camMeasuredPosition);


            } catch (NoSuchMethodException ex) {
                ErrorManager.getDefault();
            }
            sheet.put(set);
            return sheet;
        }
    }
}
