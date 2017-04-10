/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
import java.util.Observable;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.loaders.DataFolder;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author ybrise
 */
public class PCTNodePovray extends PCTNodeProject {

    public PCTNodePovray(PCTProject project) {
        super(new PCTChildrenDirectoryMerge(project),
                new ProxyLookup(
                Lookups.singleton(project),
                DataFolder.findFolder(project.getPovrayFolder(true)).getNodeDelegate().getLookup()),
                project);
        setDisplayName("Povray Scripts");
        setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_red.gif");
    }

    /*
     @Override
     public Image getIcon(int type) {
     return ImageUtilities.loadImage(
     "com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_red.gif");
     }
    
     @Override
     public Image getOpenedIcon(int type) {
     return ImageUtilities.loadImage(
     "com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_red.gif");
     }
     */
    @Override
    public Action[] getActions(boolean popup) {
        Action[] actions = super.getActions(popup);
        BundlerService bundler = project.getLookup().lookup(BundlerService.class);
        Action[] result;
        if (bundler != null && actions.length > 0) { //should always be > 0
            PCTProject.PCTProjectState state = project.getLookup().lookup(PCTProject.PCTProjectState.class);
            Action genPovAction = new PCTActionGeneratePovrayScripts(project);
            genPovAction.setEnabled(state.getProgress() < PCTProject.PCTProjectState.PCT_PROJECT_PROGRESS_BUNDLED ? false : true);
            Action renderAllAction = new PCTActionRenderAll(project);
            renderAllAction.setEnabled(state.getProgress() < PCTProject.PCTProjectState.PCT_PROJECT_PROGRESS_POVRAYED ? false : true);
            result = new Action[actions.length + 3];
            result[0] = genPovAction;
            result[1] = renderAllAction;
            result[2] = null;
            for (int i = 3; i < actions.length + 3; ++i) {
                result[i] = actions[i - 3];
            }
        } else {
            //Isolated file in the favorites window or something
            result = actions;
        }
        return result;
    }

    @Override
    protected Sheet createSheet() {

        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        RenderProperties renderProperties = project.getLookup().lookup(RendererService.class).getProperties();

        try {
            Property widthProp = new PropertySupport.Reflection(renderProperties, int.class, "getWidth", "pushWidth");
            Property heightProp = new PropertySupport.Reflection(renderProperties, int.class, "getHeight", "pushHeight");
            Property qualityProp = new PropertySupport.Reflection(renderProperties, int.class, "getQuality", "pushQuality");
            Property antiAliasingProp = new PropertySupport.Reflection(renderProperties, float.class, "getAntiAliasing", "pushAntiAliasing");
           
            widthProp.setName("width");
            heightProp.setName("height");
            qualityProp.setName("quality");
            antiAliasingProp.setName("anti aliasing");
            set.put(widthProp);
            set.put(heightProp);
            set.put(qualityProp);
            set.put(antiAliasingProp);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }
        sheet.put(set);

        return sheet;
    }
    
    @Override
    public final void update(Observable o, Object arg) {
        PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        PCTProject.NodeColor nodeColor = projectState.getNodeColorPovray();
        if (nodeColor == PCTProject.NodeColor.GREEN) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_green.gif");
        } else if (nodeColor == PCTProject.NodeColor.YELLOW) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_yellow.gif");
        } else if (nodeColor == PCTProject.NodeColor.ORANGE) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_orange.gif");
        } else if (nodeColor == PCTProject.NodeColor.RED) {
            setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_red.gif");
        }
    }
}
