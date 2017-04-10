/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.geometry;

import java.util.List;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.openide.windows.OutputWriter;

/**
 *
 * @author ybrise
 */
public abstract class Transformation {

    /**
     * This function takes a list of 3D points and transforms them according to
     * the internally stored transformation.
     *
     * @param list List of input points to transform
     * @return Transformed list of points.
     */
    public abstract void transform(List<Point3d> list);

    public abstract void transform(Point3d point);

    public abstract void transform_rotation(Matrix3d matrix);

    public abstract void transform_camera(Matrix3d matrix, Point3d location);

    public abstract Vector3d getTranslation();

    /**
     * Print the transformation in text mode.
     *
     * @param out
     */
    public abstract void print(OutputWriter out);
}
