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
public interface ViewService {

    boolean isRendered(FileObject file);

    boolean isUpToDate(FileObject file);

    void view(FileObject file);
}
