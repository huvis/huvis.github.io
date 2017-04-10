/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.Color;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author ybrise
 */
public class PCTNodeInput extends AbstractNode implements Observer {

    private final PCTProject project;

    /**
     *
     * @param project
     */
    public PCTNodeInput(PCTProject project) {
        super(new PCTChildrenFileFiltered(
                DataFolder.findFolder(project.getInputFolder(true)).getNodeDelegate(),
                new PCTCustomFileFilter(Arrays.asList("." + PCTSettings.EXTENSION_INPUT_TYPE))),
                //The projects system wants the project in the Node's lookup.
                //NewAction and friends want the original Node's lookup.
                //Make a merge of both:
                new ProxyLookup(
                Lookups.singleton(project),
                DataFolder.findFolder(project.getInputFolder(true)).getNodeDelegate().getLookup()));
        this.project = project;
        setDisplayName("Scene Pictures");
        update(null, null); // we call update instead of setting the icon manually
    }

    /*
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(
                "com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_red.gif");
    }*/

    /**
     *
     * @return
     */
    @Override
    protected Sheet createSheet() {

        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        BundlerProperties settings = project.getLookup().lookup(BundlerService.class).getProperties();
        
        try {
            //Property camColorProp = new PropertySupport.Reflection(settings, Color.class, "getCamColor", "setCamColor");
            Property camColorProp = new PropertySupport.Reflection(settings, Color.class, "getCamColor", "pushCamColor");
            camColorProp.setName("camColor");
            set.put(camColorProp);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }
        sheet.put(set);
        return sheet;
    }

    /**
     *
     * @param popup
     * @return
     */
    @Override
    public Action[] getActions(boolean popup) {
        Action[] actions = super.getActions(popup);
        //BundlerService bundler =
        //        (BundlerService) getFromProject(BundlerService.class);
        Action[] result;
        if (/*bundler != null &&*/actions.length > 0) { //should always be > 0
            //Action bundlerAction = new PCTActionBundler(bundler, this);
            result = new Action[actions.length + 1];
            for (int i = 0; i < actions.length; ++i) {
                result[i] = actions[i];
            }
            //result[actions.length] = bundlerAction;
        } else {
            //Isolated file in the favorites window or something
            result = actions;
        }
        return result;
    }

    @Override
    public final void update(Observable o, Object arg) {
        FileObject[] inputFiles = project.getInputPictures();
        if (inputFiles.length <= 0) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_red.gif");
        } else {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_green.gif");
        }
    }
}
