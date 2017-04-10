package com.vanamco.huvis.modules.toolbox.geometry;

import com.vanamco.huvis.modules.toolbox.povRelated.PovrayParser;
import java.awt.Color;
import javax.vecmath.Point3d;

/**
 *
 */
public class Sphere {

    private Point3d center;
    private double radius;
    //private String color;
    private Color color;

    public Sphere(double x, double y, double z) {
        this(x, y, z, 1, "rgb <1,1,1>");
    }

    public Sphere(double x, double y, double z, double radius) {
        this(x, y, z, radius, "rgb <1,1,1>");
    }

    public Sphere(double x, double y, double z, double radius, String colorString) {
        this(x, y, z, radius, PovrayParser.parseColorString(colorString));
    }

    public Sphere(double x, double y, double z, double radius, Color color) {
        center = new Point3d(x, y, z);
        this.radius = radius;
        this.color = color;
    }

    // Attention: this doesn't really allow for proper affine transformation,
    // because the only thing we do is transform the center of the sphere
    // according to the input argument. The radius and in particular the shape
    // of the sphere are not affected.
    public void transform(Transformation transformation) {
        transformation.transform(center);
    }

    public String toPov() {
        return "sphere { \n <" + center.x + ", " + center.y + ", " + center.z + ">, " + radius + "\ntexture{pigment{" + PovrayParser.toPov(color) + "}}\n}\n";
    }
}
