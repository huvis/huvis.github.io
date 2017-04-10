/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.povfile;

import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
import com.vanamco.huvis.modules.pctmainproject.PCTProject;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 */
public class RendererAction extends AbstractAction implements Runnable {

    private final RendererService renderer;
    private final PovrayDataNode node;
    private final PCTProject project;

    public RendererAction(PCTProject project, PovrayDataNode node) {
        this.project = project;
        this.renderer = project.getLookup().lookup(RendererService.class);
        this.node = node;
        putValue(NAME, "Render"); // display name
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RequestProcessor processor = new RequestProcessor("Render Action", 100, true);
        processor.post(this);
        //RequestProcessor.getDefault().post(this);
    }

    @Override
    public void run() {
        
        // Do RENDER for the file requested
        DataObject ob = node.getDataObject();
        FileObject toRender = ob.getPrimaryFile();
        RenderProperties mySettings = renderer.getProperties();
        FileObject image = renderer.render(toRender, mySettings);
        
        // opening the rendered file
        /*
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
        
       
        
        // Now, produce the final overlay. We do this for all images that have
        // been rendered actually
        
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Output] assemble final output");
        
        
        PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);

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
        io_wf.getOut().println(" [Output] final vizualizations created");
        
        
    }
}
