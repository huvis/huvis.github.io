/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author ybrise
 */
public class PCTChildrenDirectoryMerge extends Children.Keys<FileObject> implements FileChangeListener {

    private PCTProject project;

    public PCTChildrenDirectoryMerge(PCTProject project) {
        super();
        this.project = project;
        FileObject inputDir = project.getInputFolder(true);
        inputDir.addRecursiveListener(this);
        FileObject povrayDir = project.getPovrayFolder(true);
        povrayDir.addRecursiveListener(this);
    }

    @Override
    protected void addNotify() {

        List<FileObject> keys = new ArrayList<FileObject>();
        List<String> fileEndings;

        fileEndings = new ArrayList<String>();
        fileEndings.add(".pov");
        fileEndings.add(".inc");
        PCTCustomFileFilter fileFilter = new PCTCustomFileFilter(fileEndings);


        // get files from input directory
        FileObject inputDir = project.getInputFolder(true);
        for (FileObject candidate : inputDir.getChildren()) {
            if (fileFilter.accept(FileUtil.toFile(candidate))) {
                keys.add(candidate);
            }
        }

        // get files from the povray directory
        FileObject povrayDir = project.getPovrayFolder(true);
        for (FileObject candidate : povrayDir.getChildren()) {
            if (fileFilter.accept(FileUtil.toFile(candidate))) {
                keys.add(candidate);
            }
        }

        Collections.sort(keys, new KeySorter());

        /*
         JOptionPane test = new JOptionPane();
         test.get
         JOptionPane.  
         */


        setKeys(keys);

    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }

    @Override
    protected Node[] createNodes(FileObject fo) {

        Node node = null;

        try {
            DataObject dataObject = DataObject.find(fo);
            node = dataObject.getNodeDelegate();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        Node[] ret = new Node[1];
        ret[0] = node;

        return ret;

    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        addNotify();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        addNotify();
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        addNotify();
    }

    @Override
    public void fileRenamed(FileRenameEvent fre) {
        addNotify();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fae) {
    }

    private class KeySorter implements Comparator<FileObject> {

        @Override
        public int compare(FileObject fo1, FileObject fo2) {
            return fo1.getName().compareTo(fo2.getName());
        }
    }
}
