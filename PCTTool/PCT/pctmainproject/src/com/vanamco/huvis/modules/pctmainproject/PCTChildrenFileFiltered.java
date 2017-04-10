/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author ybrise
 */
public class PCTChildrenFileFiltered extends FilterNode.Children {

    private final FileFilter fileFilter;

    public PCTChildrenFileFiltered(Node owner, FileFilter fileFilter) {
        super(owner);
        this.fileFilter = fileFilter;
    }

    /*
     @Override
     protected Node copyNode(Node original) {
     return new ScenesNode(original, fileFilter);
     }
     */
    @Override
    protected Node[] createNodes(Node object) {
        List<Node> result = new ArrayList<Node>();

        for (Node node : super.createNodes(object)) {
            FileObject fileObject = node.getLookup().lookup(FileObject.class);
            if (fileObject != null) {
                File file = FileUtil.toFile(fileObject);
                if (fileFilter.accept(file)) {
                    result.add(node);
                }
            }
        }

        return result.toArray(new Node[result.size()]);
    }
}
