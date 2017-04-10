/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
import java.util.Arrays;
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
public class PCTNodeVisualization extends PCTNodeProject {

    public PCTNodeVisualization(PCTProject project) {
        super(new PCTChildrenFileFiltered(
                DataFolder.findFolder(project.getOutputFolder(true)).getNodeDelegate(),
                new PCTCustomFileFilter(Arrays.asList(".jpg", ".png", ".tif"))),
                //The projects system wants the project in the Node's lookup.
                //NewAction and friends want the original Node's lookup.
                //Make a merge of both:
                new ProxyLookup(
                Lookups.singleton(project),
                DataFolder.findFolder(project.getOutputFolder(true)).getNodeDelegate().getLookup()),
                project);
        setDisplayName("Visualization");
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
        //BundlerService bundler = (BundlerService)getFromProject(BundlerService.class);
        Action[] result;
        if (actions.length > 0) { //should always be > 0
            //Action genPovAction = new PCTActionGeneratePovrayScripts(bundler, this);
            result = new Action[actions.length + 1];
            //result[0] = new PCTActionGenerateVisualization(project);
            result[0] = null;
            for (int i = 1; i < actions.length + 1; ++i) {
                result[i] = actions[i - 1];
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
        PCTProject.NodeColor nodeColor = projectState.getNodeColorVisualization();
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
