/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.geometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import org.openide.windows.OutputWriter;

/**
 *
 * @author ybrise
 */
public class TransformationJAG3D extends Transformation {

    private Matrix3d rotation;
    private double scaleX;
    private double scaleY;
    private double scaleZ;
    private Vector3d translation;

    /**
     *
     */
    public TransformationJAG3D() {
        translation = new Vector3d(0.0, 0.0, 0.0);
        scaleX = 1.0;
        scaleY = 1.0;
        scaleZ = 1.0;
        rotation = new Matrix3d();
        rotation.setIdentity();
    }

    /**
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public TransformationJAG3D(File file) throws FileNotFoundException, IOException, Exception {
        BufferedReader in = new BufferedReader(new FileReader(file));

        double tx;
        double ty;
        double tz;
        double rx;
        double ry;
        double rz;
        double sx;
        double sy;
        double sz;

        //String line = in.readLine().trim();

        String[] atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Tx")) {
            throw new Exception();
        }
        tx = new Double(atoms[2].trim());

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Ty")) {
            throw new Exception();
        }
        ty = new Double(atoms[2].trim());

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Tz")) {
            throw new Exception();
        }
        tz = new Double(atoms[2].trim());

        // discard lines with q0, q1, q2, q3
        in.readLine();
        in.readLine();
        in.readLine();
        in.readLine();

        atoms = in.readLine().trim().split("\\s+");

        if (!atoms[0].equals("Mx")) {
            throw new Exception();
        }
        sx = new Double(atoms[2]);

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("My")) {
            throw new Exception();
        }
        sy = new Double(atoms[2]);

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Mz")) {
            throw new Exception();
        }
        sz = new Double(atoms[2]);

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Rx")) {
            throw new Exception();
        }
        rx = new Double(atoms[2]);

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Ry")) {
            throw new Exception();
        }
        ry = new Double(atoms[2]);

        atoms = in.readLine().trim().split("\\s+");
        if (!atoms[0].equals("Rz")) {
            throw new Exception();
        }
        rz = new Double(atoms[2]);

        init(tx, ty, tz, rx, ry, rz, sx, sy, sz);
    }

    /**
     *
     * @param tx
     * @param ty
     * @param tz
     * @param rx
     * @param ry
     * @param rz
     * @param sx
     * @param sy
     * @param sz
     */
    public void init(double tx, double ty, double tz, double rx, double ry, double rz, double sx, double sy, double sz) {
        translation = new Vector3d(tx, ty, tz);
        scaleX = sx;
        scaleY = sy;
        scaleZ = sz;
        rotation = new Matrix3d();
        Matrix3d temp = new Matrix3d();

        rotation.setIdentity();
        temp.rotZ(rz);
        rotation.mul(temp);
        temp.rotY(ry);
        rotation.mul(temp);
        temp.rotX(rx);
        rotation.mul(temp);
        rotation.transpose();
    }

    @Override
    public void transform(List<Point3d> list) {
        for (Iterator<Point3d> it = list.iterator(); it.hasNext();) {
            Point3d point = it.next();
            rotation.transform(point);
            scale(point);
            point.add(translation);
        }
    }

    /**
     *
     * @param point
     */
    @Override
    public void transform(Point3d point) {
        rotation.transform(point);
        scale(point);
        point.add(translation);
    }

    /**
     *
     * @param matrix
     */
    @Override
    public void transform_rotation(Matrix3d matrix) {
        //rotation.mul(matrix);
        matrix.mulTransposeRight(matrix, rotation);
        //matrix.transpose();
        /*
         matrix.m00 = matrix.m00 / scaleX;
         matrix.m01 = matrix.m00 / scaleX;
         matrix.m02 = matrix.m00 / scaleX;
         matrix.m10 = matrix.m00 / scaleY;
         matrix.m11 = matrix.m00 / scaleY;
         matrix.m12 = matrix.m00 / scaleY;
         matrix.m20 = matrix.m00 / scaleZ;
         matrix.m21 = matrix.m00 / scaleZ;
         matrix.m22 = matrix.m00 / scaleZ;*/
    }

    /**
     *
     * @param matrix
     * @param point
     */
    @Override
    public void transform_camera(Matrix3d matrix, Point3d point) {

        matrix.mulTransposeRight(matrix, rotation);
        rotation.transform(translation);
        translation.scaleAdd(-1.0, translation, point);

        //rotation.transform(point);
        //scale(point);
        //point.add(translation);
    }

    @Override
    public void print(OutputWriter out) {
        out.println("Translation: (" + translation.x + ", " + translation.y + ", " + translation.z + ")");
        out.println("Scaling: (" + scaleX + ", " + scaleY + ", " + scaleZ + ")");
        out.println("Rotation matrix:\n" + rotation);
    }

    // Note that the following function clashes with Tuple3D.scale, which is
    // why it is private. The following allows for skewed scaling, with different
    // factors along the three axes.
    private void scale(Tuple3d point) {
        point.x = scaleX * point.x;
        point.y = scaleY * point.y;
        point.z = scaleZ * point.z;
    }

    /**
     *
     * @return
     */
    @Override
    public Vector3d getTranslation() {
        return translation;
    }
}
