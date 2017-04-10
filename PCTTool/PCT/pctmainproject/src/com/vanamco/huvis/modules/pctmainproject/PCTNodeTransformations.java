/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.api.bundlerapi.BundlerService;
import java.awt.Color;
import java.util.Arrays;
import java.util.Observable;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author ybrise
 */
public class PCTNodeTransformations extends PCTNodeProject {

    public PCTNodeTransformations(PCTProject project) {
        super(new PCTChildrenFileFiltered(
                DataFolder.findFolder(project.getInputFolder(true)).getNodeDelegate(),
                new PCTCustomFileFilter(Arrays.asList(".xml", ".txt", ".xml", ".jag3d"))),
                //The projects system wants the project in the Node's lookup.
                //NewAction and friends want the original Node's lookup.
                //Make a merge of both:
                new ProxyLookup(
                Lookups.singleton(project),
                DataFolder.findFolder(project.getInputFolder(true)).getNodeDelegate().getLookup()),
                project);
        this.project = project;
        setDisplayName("Transformation");
        //setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_red.gif");
        update(null, null); // we call update instead of setting the icon manually
    }

    @Override
    protected Sheet createSheet() {

        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        BundlerProperties settings = project.getLookup().lookup(BundlerService.class).getProperties();

        try {
            //Property camColorProp = new PropertySupport.Reflection(settings, Color.class, "getCamColor", "setCamColor");
            Node.Property camColorProp = new PropertySupport.Reflection(settings, Color.class, "getCamColor", "pushCamColor");
            camColorProp.setName("camColor");
            set.put(camColorProp);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }
        sheet.put(set);
        return sheet;
    }

    @Override
    public Action[] getActions(boolean popup) {
        Action[] actions = super.getActions(popup);
        Action[] result;
        if (/*bundler != null &&*/actions.length > 0) { //should always be > 0
            PCTProject.PCTProjectState state = project.getLookup().lookup(PCTProject.PCTProjectState.class);
            Action transformationAction = new PCTActionTransformation(project);
            transformationAction.setEnabled(state.hasTransformationAttached() ? false : true);
            Action removeTansfromationAction = new PCTActionRemoveTransformation(project);
            removeTansfromationAction.setEnabled(state.hasTransformationAttached() ? true : false);
            result = new Action[actions.length + 3];
            result[0] = transformationAction;
            result[1] = removeTansfromationAction;
            result[2] = null;
            for (int i = 0; i < actions.length; ++i) {
                result[i + 2] = actions[i];
            }
        } else {
            //Isolated file in the favorites window or something
            result = actions;
        }
        return result;
    }

    @Override
    public final void update(Observable o, Object arg) {
        PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        PCTProject.NodeColor nodeColor = projectState.getNodeColorTransformation();
        if (nodeColor == PCTProject.NodeColor.GREEN) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_green.gif");
        } else if (nodeColor == PCTProject.NodeColor.YELLOW) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_yellow.gif");
        } else if (nodeColor == PCTProject.NodeColor.ORANGE) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_orange.gif");
        } else if (nodeColor == PCTProject.NodeColor.RED) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_static_node_red.gif");
        }
    }
}
