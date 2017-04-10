/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.api.bundlerapi.scene.Camera;
import com.vanamco.huvis.api.bundlerapi.scene.Scene;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author ybrise
 */
public class PCTChildrenCameras extends Children.Keys<Integer> {

    private PCTProject project;
    private Scene scene;

    public PCTChildrenCameras(PCTProject project) {
        super();
        this.project = project;
        this.scene = project.getLookup().lookup(BundlerService.class).getScene(project.getBundlerFolder(true));
    }

    @Override
    protected void addNotify() {

        scene = project.getLookup().lookup(BundlerService.class).getScene(project.getBundlerFolder(true));

        if (scene != null) {
            List<Integer> indices = new ArrayList<Integer>();
            for (int i = 0; i < scene.getCameras().length; ++i) {
                indices.add(i);
            }
            Collections.sort(indices);

            setKeys(indices);
        } else {
            setKeys(Collections.EMPTY_SET);
        }

    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    @Override
    protected Node[] createNodes(Integer index) {

        Node[] ret = new Node[1];

        Camera[] cameras = scene.getCameras();
        if (index >= 0 && index < cameras.length) {
            cameras[index].setId(index);
            ret[0] = cameras[index].getNodeDelegate();
        }
        return ret;
    }
}
