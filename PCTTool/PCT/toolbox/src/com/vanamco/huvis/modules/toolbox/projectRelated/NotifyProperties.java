/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.toolbox.projectRelated;

import java.util.Properties;
import org.netbeans.spi.project.ProjectState;

/**
 *
 * @author ybrise
 */
public class NotifyProperties extends Properties {

    private final ProjectState state;

    public NotifyProperties(ProjectState state) {
        this.state = state;
    }

    // careful! we should still use setProperty to modify entries, because
    // the put method is not type-safe. put is the underlying function
    // of setProperties though, so we don't need to override setProperties.
    // This is used to circumvent markModified(), for the very special case
    // of loading a project, in which case the project musn't be marked as
    // modified as checked by an assertion. Use this function only if you
    // DON'T want the project to be marked as outdated by setting a particular
    // property's value.
    @Override
    public Object put(Object key, Object val) {

        Object result = super.put(key, val);
        return result;

    }
    
    // This function DOES properly invoke markModified if changes were made.
    public Object pushProperty(String key, String val) {
        Object result = super.put(key, val);
        
        if (((result == null) != (val == null)) || (result != null
                && val != null && !val.equals(result)) && state != null) {
            state.markModified();
        }
        
        return result;
    }
}
