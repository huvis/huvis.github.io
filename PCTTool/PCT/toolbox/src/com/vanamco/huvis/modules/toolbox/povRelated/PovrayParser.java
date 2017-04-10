/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.povRelated;

import java.awt.Color;

/**
 *
 * @author ybrise
 */
public class PovrayParser {

    public PovrayParser() {
    }

    public static String toPov(Color color) {
        float[] colorComponents = color.getRGBColorComponents(null);
        
        return "rgb <" + colorComponents[0] + "," + colorComponents[1] + "," + colorComponents[2] + ">";
    }

    public static Color parseColorString(String colorString) {
        String[] splitColor = colorString.split("[,]");
        splitColor[0] = splitColor[0].split("[<]")[1];
        splitColor[2] = splitColor[2].split("[>]")[0];
        return new Color(new Float(splitColor[0]), new Float(splitColor[1]), new Float(splitColor[2]));
    }
}
