/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.api.bundlerapi.BundlerService;
import java.util.Observable;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataFolder;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author ybrise
 */
public class PCTNodeReconstruction extends PCTNodeProject {

    //private final PCTProject project;
    //private final ReconstructionSettings settings;
    private final ExplorerManager manager = new ExplorerManager();

    public PCTNodeReconstruction(PCTProject project) {
        super(new PCTChildrenCameras(project),
                new ProxyLookup(
                Lookups.singleton(project),
                DataFolder.findFolder(project.getBundlerFolder(true)).getNodeDelegate().getLookup()),
                project);
        //this.project = project;
        setDisplayName("Reconstruction");
        setIconBaseWithExtension("com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_red.gif");
        //settings = new ReconstructionSettings();
    }

    @Override
    public Action[] getActions(boolean popup) {
        Action[] actions = super.getActions(popup);
        BundlerService bundler = project.getLookup().lookup(BundlerService.class);
        Action[] result;
        if (bundler != null && actions.length > 0) { //should always be > 0
            Action bundlerAction = new PCTActionBundler(project);
            bundlerAction.setEnabled((project.hasInputPictures() ? true : false));
            result = new Action[actions.length + 2];
            result[0] = bundlerAction;
            result[1] = null;
            for (int i = 2; i < actions.length + 2; ++i) {
                result[i] = actions[i - 2];
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
        BundlerProperties settings = project.getLookup().lookup(BundlerService.class).getProperties();

        try {
            Property camOpeningAngleProp = new PropertySupport.Reflection(settings, float.class, "getOpeningAngle", "pushOpeningAngle");
            camOpeningAngleProp.setName("dummy");
            Property featureSizeProp = new PropertySupport.Reflection(settings, float.class, "getFeatureSize", "pushFeatureSize");
            featureSizeProp.setName("feature size");
            
            set.put(camOpeningAngleProp);
            set.put(featureSizeProp);
        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }
        sheet.put(set);
        return sheet;
    }

    @Override
    public final void update(Observable o, Object arg) {
        PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        PCTProject.NodeColor nodeColor = projectState.getNodeColorReconstruction();
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

    /*
    public class ReconstructionSettings {

        private Color camColor;

        public ReconstructionSettings() {
            camColor = Color.CYAN;
        }

        public Color getCamColor() {
            return camColor;
        }

        public void setCamColor(Color camColor) {
            this.camColor = camColor;
        }
    }*/
}
