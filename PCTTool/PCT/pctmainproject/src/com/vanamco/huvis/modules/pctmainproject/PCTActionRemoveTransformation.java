/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 */
/**
 * /** Action to remove a loaded transformation. If no transformation is loaded
 * this action has no effect.
 */
public class PCTActionRemoveTransformation extends AbstractAction implements Presenter.Popup, Runnable {

    private final PCTProject project;
    
    public PCTActionRemoveTransformation(PCTProject project) {
        this.project = project;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Transformation Action", 1, true);
        processor.post(this);
    }

    @NbBundle.Messages("LBL_RemoveTransformation=Remove Transformation")
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenuItem(this);

        //Set the menu's label
        result.setText(Bundle.LBL_RemoveTransformation());
        return result;
    }

    @Override
    public void run() {
        BundlerService bundlerService = project.getLookup().lookup(BundlerService.class);
        bundlerService.removeTransformation();
        PCTProject.PCTProjectState state = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        state.removeTransformation();
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Transformation] transformation file " + PCTSettings.TRANSFORMATION_FILE_NAME + " unloaded");
    }
}
