/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
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
public class PCTActionRenderAll extends AbstractAction implements Presenter.Popup, Runnable {

    private final PCTProject project;

    public PCTActionRenderAll(PCTProject project) {
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Render All Action", 100, true);
        processor.post(this);
        //RequestProcessor.getDefault().post(this);
    }

    @NbBundle.Messages("LBL_RenderAll=Render All .pov Files")
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenuItem(this);
        //Set the menu's label
        result.setText(Bundle.LBL_RenderAll());
        return result;
    }

    @Override
    public void run() {
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [PCT] render all Povray scripts");
        
        FileObject povrayFolder = project.getPovrayFolder(true);
        RendererService renderer = project.getLookup().lookup(RendererService.class);
        for (FileObject file : povrayFolder.getChildren()) {
            if (file.getExt().equals(PCTSettings.EXTENSION_POVRAY_TYPE)) {
                RenderProperties mySettings = renderer.getProperties();
                FileObject image = renderer.render(file, mySettings);
                // The following is disabled. We don't necessarily want to
                // open the rendered files, because they are just an intermediate
                // product.
                /* // open files
                if (image != null) {
                    try {
                        //Try to open the file:
                        DataObject dob = DataObject.find(image);
                        Node n = dob.getNodeDelegate();
                        OpenCookie ck = n.getLookup().lookup(OpenCookie.class);
                        if (ck != null) {
                            ck.open();
                        }
                    } catch (DataObjectNotFoundException e) {
                        //Should never happen
                        Exceptions.printStackTrace(e);
                    }
                }*/
            }
        }
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [PCT] rendering successful");
        
        // generate visualizations
        //RequestProcessor processor = new RequestProcessor("Render All Action - subthread for visualization", 100, true);
        //processor.post(new PCTActionGenerateVisualization(project));
        PCTActionGenerateVisualization visualizationAction = new PCTActionGenerateVisualization(project);
        visualizationAction.run();
        
        // adjust project state
        PCTProject.PCTProjectState state = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        state.pushProgress(PCTProject.PCTProjectState.PCT_PROJECT_PROGRESS_RENDERED_FULL);
        
    }
}
