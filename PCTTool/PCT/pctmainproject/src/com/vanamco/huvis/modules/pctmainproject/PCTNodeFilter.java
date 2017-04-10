/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import java.awt.Image;
import java.util.List;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author ybrise
 */
public class PCTNodeFilter extends FilterNode {

    final PCTProject project;

    public PCTNodeFilter(Node node, PCTProject project, List<String> fileEndings)
            throws DataObjectNotFoundException {
        //super(node, new FilterNode.Children(node),
        super(node, new PCTChildrenFileFiltered(node, new PCTCustomFileFilter(fileEndings)),
                //The projects system wants the project in the Node's lookup.
                //NewAction and friends want the original Node's lookup.
                //Make a merge of both:
                new ProxyLookup(
                Lookups.singleton(project),
                node.getLookup()));
        disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION
                | DELEGATE_DESTROY);
        this.project = project;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(
                "com/vanamco/huvis/modules/pctmainproject/resources/pct_workflow_node_red.gif");
    }

    /*
     @Override
     public String getDisplayName() {
     return project.getProjectDirectory().getName();
     }*/
}
