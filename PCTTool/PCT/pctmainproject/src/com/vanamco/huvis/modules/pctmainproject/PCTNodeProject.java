/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import java.util.Observable;
import java.util.Observer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ybrise
 */
public abstract class PCTNodeProject extends AbstractNode implements Observer {

    /**
     *
     */
    protected PCTProject project;

    /**
     *
     * @param project
     */
    protected PCTNodeProject(PCTProject project) {
        super(Children.LEAF, Lookups.singleton(project));
        this.project = project;
    }

    /**
     *
     * @param children
     * @param lookup
     * @param project
     */
    protected PCTNodeProject(Children children, Lookup lookup, PCTProject project) {
        super(children, lookup);
        this.project = project;
    }

    
    @Override
    public void update(Observable o, Object arg) {
        throw new UnsupportedOperationException( "This should never be called. Use for setting icon in extending classes." );
    }
}
