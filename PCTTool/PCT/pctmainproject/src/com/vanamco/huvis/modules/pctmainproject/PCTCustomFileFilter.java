/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import java.io.File;
import java.util.List;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author ybrise
 */
public class PCTCustomFileFilter extends FileFilter {

    private final List<String> fileEndings;

    public PCTCustomFileFilter(List<String> fileEndings) {
        this.fileEndings = fileEndings;
    }

    @Override
    public boolean accept(File file) {
        boolean ret = false;
        int i = 0;
        while (!ret && i < fileEndings.size()) {
            if (file.getName().endsWith(fileEndings.get(i).toLowerCase())
                    || file.getName().endsWith(fileEndings.get(i).toUpperCase())) {
                ret = true;
            }
            ++i;
        }
        return ret;
    }

    @Override
    public String getDescription() {
        return "Filter " + fileEndings.toString() + " files only.";
    }
}
