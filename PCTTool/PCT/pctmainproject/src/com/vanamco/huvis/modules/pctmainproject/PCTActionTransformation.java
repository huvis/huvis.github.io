/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.modules.toolbox.geometry.Transformation;
import com.vanamco.huvis.modules.toolbox.geometry.TransformationJAG3D;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 */
public class PCTActionTransformation extends AbstractAction implements Presenter.Popup, Runnable {
    
    private final PCTProject project;
    
    public PCTActionTransformation(PCTProject project) {
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Transformation Action", 1, true);
        processor.post(this);
        //RequestProcessor.getDefault().post(this);
    }

    @NbBundle.Messages("LBL_Transformation=Apply Transformation")
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenuItem(this);

        //Set the menu's label
        result.setText(Bundle.LBL_Transformation());
        return result;
    }

    @Override
    public void run() {
        // find transformation parameters
        // as of now, just read them from the .jag3d file that is (should be)
        // provided
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Transformation] try to load external tranformation file " + PCTSettings.TRANSFORMATION_FILE_NAME);

        FileObject fo = project.getTransformationFile();

        if (fo == null) {
            // report failure
            io_wf.select();
            io_wf.getOut().print(PCTSettings.timestamp());
            io_wf.getOut().println(" [Transformation] ERROR did not find " + PCTSettings.TRANSFORMATION_FILE_NAME);
        } else {
            try {
                Transformation transformation = new TransformationJAG3D(new File(fo.getPath()));
                // output the loaded transformation to the Transformation output tab
                InputOutput io = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_TRANSFORMATION, false);
                io.select();
                io.getOut().println("Transformation loaded:");
                transformation.print(io.getOut());
                io.getOut().println();
                // apply transformation to the bundler setting
                //BundlerService bundlerService = (BundlerService) node.getFromProject(BundlerService.class);
                BundlerService bundlerService = project.getLookup().lookup(BundlerService.class);
                bundlerService.setTransformation(transformation);
                // modify project state
                //PCTProject.PCTProjectState projectState = (PCTProject.PCTProjectState) node.getFromProject(PCTProject.PCTProjectState.class);
                PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
                projectState.confirmTransformation();
                // report success
                io_wf.select();
                io_wf.getOut().print(PCTSettings.timestamp());
                io_wf.getOut().println(" [Transformation] successfully loaded " + PCTSettings.TRANSFORMATION_FILE_NAME);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
