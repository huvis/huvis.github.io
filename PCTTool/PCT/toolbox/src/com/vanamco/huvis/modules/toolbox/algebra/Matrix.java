/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.algebra;

import java.io.PrintStream;

/**
 *
 * @author ybrise
 * @author fmueller
 */
public class Matrix {

    public Matrix() {
    }

    /**
     *
     * @param source
     * @return
     */
    public static double[][] transposeMatrix(double[][] source) {

        double[][] retVal = new double[source[0].length][source.length];

        for (int i = 0; i < retVal.length; i++) {
            for (int j = 0; j < retVal[0].length; j++) {
                retVal[i][j] = source[j][i];
            }
        }
        return retVal;
    }

    /**
     *
     * @param matrix
     */
    public static void printMatrix(double[][] matrix, PrintStream out) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                out.print(matrix[i][j] + "  ");
            }
        }

    }

    /**
     *
     * @param matrix
     * @param scalar
     * @return
     */
    public static double[][] matrixScalar(double[][] matrix, double scalar) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = scalar * matrix[i][j];
            }
        }
        return matrix;
    }

    /**
     *
     * @param matrix
     * @param vector
     * @return
     */
    public static double[] matrixVector(double[][] matrix, double[] vector) {
        double[] retVal = new double[vector.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                retVal[i] += matrix[i][j] * vector[j];
            }
        }
        return retVal;
    }
}
