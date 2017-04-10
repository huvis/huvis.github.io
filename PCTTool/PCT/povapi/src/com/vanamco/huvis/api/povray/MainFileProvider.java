/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.api.povray;

import org.openide.filesystems.FileObject;

/**
 *
 * @author ybrise
 */
public abstract class MainFileProvider {

    public abstract FileObject getMainFile();

    public abstract void setMainFile(FileObject file);

    public boolean isMainFile(FileObject file) {
        return file.equals(getMainFile());
    }
}
