/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerService;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 *
 * @author ybrise
 */
public class PCTActionBundler extends AbstractAction implements Presenter.Popup, Runnable {

    private final PCTProject project;
    private final BundlerService bundler;
    
    /**
     *
     * @param project
     */
    public PCTActionBundler(PCTProject project) {
        this.project = project;
        this.bundler = project.getLookup().lookup(BundlerService.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Bundler Action", 1, true);
        processor.post(this);
        //RequestProcessor.getDefault().post(this);
    }

    /**
     *
     * @return
     */
    @NbBundle.Messages("LBL_Bundle=Bundle")
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenuItem(this);

        //Set the menu's label
        result.setText(Bundle.LBL_Bundle());
        return result;
    }

    @Override
    public void run() {
        bundler.bundle(project.getInputPictures(), project.getBundlerFolder(true));
        project.consolidate();
        // check if output file is present
        FileObject bundlerOutputFile = project.getBundlerOutputFile();
        if (project.isReconstructionCurrent()) {
            PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
            projectState.pushProgress(PCTProject.PCTProjectState.PCT_PROJECT_PROGRESS_BUNDLED);
        }
    }
}
