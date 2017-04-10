/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.povray;

import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author ybrise
 */
public class RendererServiceImpl extends RendererService {

    

    /**
     *
     * @param properties
     */
    public RendererServiceImpl(RenderProperties properties) {
        //this.properties = (properties == null ? new RenderProperties()  : properties);
        this.properties = properties;
    }

    /**
     *
     * @param scene
     * @param propertiesName
     * @return
     */
    @Override
    public FileObject render(FileObject scene, String propertiesName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param script
     * @param renderProperties
     * @return
     */
    @Override
    public FileObject render(FileObject script, RenderProperties renderProperties) {
        PovrayExternal pov = new PovrayExternal(this, script, renderProperties);
        FileObject result;
        try {
            result = pov.render();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            result = null;
        }
        return result;
    }

    /**
     *
     * @param scene
     * @return
     */
    @Override
    public FileObject render(FileObject scene) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return
     */
    @Override
    public FileObject render() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    static Preferences getPreferences() {
        return Preferences.userNodeForPackage(RendererServiceImpl.class);
    }
}
