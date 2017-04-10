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
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 */
public class PCTActionGeneratePovrayScripts extends AbstractAction implements Presenter.Popup, Runnable {

    private final PCTProject project;
    private final BundlerService bundler;

    public PCTActionGeneratePovrayScripts(PCTProject project) {
        this.bundler = project.getLookup().lookup(BundlerService.class);
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Generate Povray Scripts Action", 1, true);
        processor.post(this);
        //RequestProcessor.getDefault().post(this);
    }

    @NbBundle.Messages("LBL_GeneratePov=Generate Povray Scripts")
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenuItem(this);

        //Set the menu's label
        result.setText(Bundle.LBL_GeneratePov());
        return result;
    }

    @Override
    public void run() {
        //bundler.bundle(node.getScenePictures(), node.getWorkingDir());
        /*
         FileObject[] scenePictures = node.getScenePictures();
         FileObject workingDir = node.getWorkingDir();
         for (FileObject fileObject : scenePictures) {
         bundler.generatePovrayScript(fileObject, workingDir);
         }*/
        
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [PCT] generate Povray scripts from Bundler output");
        
        PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        // setup preamble = "#include \"../input/fraueli.inc\"";
        String preamble = "";
        if (projectState.hasTransformationAttached()) {
            FileObject[] includeFiles = project.getPovrayIncludeFiles();
            FileObject inputFolder = project.getInputFolder(true);
            for (FileObject file : includeFiles) {
                preamble += PCTSettings.NEWLINE;
                preamble += "#include \"";
                preamble += inputFolder.getPath() + PCTSettings.FILESEPARATOR;
                preamble += file.getNameExt();
                preamble += "\"";
            }
        }
        bundler.generatePovrayScript(project.getBundlerOutputDir(), project.getBundlerFolder(true), preamble);
        
        // adjust project state
        projectState.pushProgress(PCTProject.PCTProjectState.PCT_PROJECT_PROGRESS_POVRAYED);
        
        
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [PCT] Povray scripts generated");
    }
}
