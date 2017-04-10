/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 */
public class PCTActionGenerateVisualization extends AbstractAction implements Presenter.Popup, Runnable {

    private final PCTProject project;
    
    public PCTActionGenerateVisualization(PCTProject project) {
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Generate Visualization Action", 1, true);
        processor.post(this);
    }

    @NbBundle.Messages("LBL_GenerateOverlay=Generate Final Output")
    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem result = new JMenuItem(this);

        //Set the menu's label
        result.setText(Bundle.LBL_GenerateOverlay());
        return result;
    }

    @Override
    public void run() {

        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Output] assemble final output");


        PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
        
        // This is not callable by the user directly anymore, but it's still good
        // practice to check the project state before looking for the rendered files.
        if (projectState.getProgress() >= PCTProject.PCTProjectState.PCT_PROJECT_PROGRESS_POVRAYED) {

            // CHECK whether renderings exist and are up-to-date
            FileObject[] scenePictures = project.getInputPictures();
            FileObject[] renderedPictures = project.getRenderedPictures();
            FileObject[] sortedOverlays = new FileObject[scenePictures.length];

            for (int i = 0; i < scenePictures.length; ++i) {
                for (FileObject file : renderedPictures) {
                    if (file.getName().equals(scenePictures[i].getName())) {
                        sortedOverlays[i] = file;
                        break;
                    } else {
                        sortedOverlays[i] = null;
                    }
                }
            }

            // RENDER if necessary
            // well for now, let us just disregard unrendered pics, because it takes
            // a long time. This has to be re-thought at some point maybe.


            // PRODUCE overlays in vizualization directory
            // load source images
            // TODO: possibly use faster image manipulation library
            for (int i = 0; i < scenePictures.length; ++i) {
                BufferedImage primary = null;
                BufferedImage overlay = null;
                BufferedImage result = null;
                try {
                    if (sortedOverlays[i] != null) {
                        primary = ImageIO.read(new File(scenePictures[i].getPath()));
                        overlay = ImageIO.read(new File(sortedOverlays[i].getPath()));
                        result = (BufferedImage) ImageUtilities.mergeImages(primary, overlay, 0, 0);
                        ImageIO.write(result, PCTSettings.EXTENSION_OUTPUT_TYPE, new File(project.getOutputFolder(true).getPath() + PCTSettings.FILESEPARATOR + scenePictures[i].getName() + "." + PCTSettings.EXTENSION_OUTPUT_TYPE));
                    }
                } catch (IOException e) {
                }
            }

            io_wf.select();
            io_wf.getOut().print(PCTSettings.timestamp());
            io_wf.getOut().println(" [Output] final visualizations created");
        } else { // project state not appropriate
            io_wf.select();
            io_wf.getOut().print(PCTSettings.timestamp());
            io_wf.getOut().println(" [Output] could not create visualizations");
        }
    }
}
