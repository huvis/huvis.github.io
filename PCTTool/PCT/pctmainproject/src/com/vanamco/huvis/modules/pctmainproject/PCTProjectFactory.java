/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
import com.vanamco.huvis.modules.toolbox.projectRelated.NotifyProperties;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class to represent and recognize PointCloudTool projects.
 *
 * @author ybrise
 */
@ServiceProvider(service = ProjectFactory.class)
public class PCTProjectFactory implements ProjectFactory {

    @Override
    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject(PCTSettings.DIR_PROJECT) != null;
    }

    @Override
    public Project loadProject(FileObject projectRoot, ProjectState state) throws IOException {

        PCTProject project = null;


        if (isProject(projectRoot)) {

            ///PCTProjectState complexState = new PCTProjectState(state);
            //project = new PCTProject(projectRoot, complexState);
            project = new PCTProject(projectRoot, state);
            PCTProject.PCTProjectState complexState = project.getLookup().lookup(PCTProject.PCTProjectState.class);


            if (projectRoot.getFileObject(PCTSettings.DIR_PROJECT) == null) {
                throw new IOException("Project dir " + projectRoot.getPath() + " deleted,"
                        + " cannot load project");
            }

            
            // Load specific properties (basic property file is automatically
            // loaded; see PCTProject.loadProperties).
            String propsPath = projectRoot.getPath() + PCTSettings.FILESEPARATOR + PCTSettings.DIR_PROJECT + PCTSettings.FILESEPARATOR + PCTSettings.PROPFILE_BUNDLER;

            BundlerProperties bundlerProperties = new BundlerProperties(state, complexState);
            FileInputStream in = new FileInputStream(propsPath);
            bundlerProperties.load(in);
            in.close();
            BundlerService bundlerService = project.getLookup().lookup(BundlerService.class);
            bundlerService.setProperties(bundlerProperties);
            
            propsPath = projectRoot.getPath() + PCTSettings.FILESEPARATOR + PCTSettings.DIR_PROJECT + PCTSettings.FILESEPARATOR + PCTSettings.PROPFILE_RENDERER;
            RenderProperties renderProperties = new RenderProperties(state, complexState);
            in = new FileInputStream(propsPath);
            renderProperties.load(in);
            in.close();
            RendererService renderService = project.getLookup().lookup(RendererService.class);
            renderService.setProperties(renderProperties);
           


            // consolidate project properties (bundler properties override render settings)
            project.consolidate();
        }
        
        return project;
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {

        FileObject projectRoot = project.getProjectDirectory();
        if (projectRoot.getFileObject(PCTSettings.DIR_PROJECT) == null) {
            throw new IOException("Project dir " + projectRoot.getPath() + " deleted,"
                    + " cannot save project");
        }

        //Force creation of temporary folders
        project.getLookup().lookup(PCTProject.class).getBundlerFolder(true);
        project.getLookup().lookup(PCTProject.class).getPovrayFolder(true);
        project.getLookup().lookup(PCTProject.class).getOutputFolder(true);

        //Find the properties file pctmainproject/project.properties,
        //creating it if necessary
        String propsPath = PCTSettings.DIR_PROJECT + PCTSettings.FILESEPARATOR + PCTSettings.PROPFILE_PROJECT;
        FileObject propertiesFile = projectRoot.getFileObject(propsPath);
        if (propertiesFile == null) {
            //Recreate the properties file if needed
            propertiesFile = projectRoot.getFileObject(PCTSettings.DIR_PROJECT).createData(PCTSettings.PROPFILE_PROJECT);
        }
        File f = FileUtil.toFile(propertiesFile);
        Properties properties = project.getLookup().lookup(NotifyProperties.class);
        properties.store(new FileOutputStream(f), "PCT Project Properties");

        // Store renderer Settings/Properties
        propsPath = PCTSettings.DIR_PROJECT + PCTSettings.FILESEPARATOR + PCTSettings.PROPFILE_RENDERER;
        propertiesFile = projectRoot.getFileObject(propsPath);
        if (propertiesFile == null) {
            propertiesFile = projectRoot.getFileObject(PCTSettings.DIR_PROJECT).createData(PCTSettings.PROPFILE_RENDERER);
        }
        RendererService rendererService = project.getLookup().lookup(RendererService.class);
        f = FileUtil.toFile(propertiesFile);
        Properties rendererProperties = rendererService.getProperties();
        rendererProperties.store(new FileOutputStream(f), "PCT Render Properties");

        // Store bundler Settings/Properties
        propsPath = PCTSettings.DIR_PROJECT + PCTSettings.FILESEPARATOR + PCTSettings.PROPFILE_BUNDLER;
        propertiesFile = projectRoot.getFileObject(propsPath);
        if (propertiesFile == null) {
            propertiesFile = projectRoot.getFileObject(PCTSettings.DIR_PROJECT).createData(PCTSettings.PROPFILE_BUNDLER);
        }
        BundlerService bundlerService = project.getLookup().lookup(BundlerService.class);
        f = FileUtil.toFile(propertiesFile);
        Properties bundlerProperties = bundlerService.getProperties();
        bundlerProperties.store(new FileOutputStream(f), "PCT Bundler Properties");

        /*
         File f2 = FileUtil.toFile(projectRoot.createData("test.xml"));
         rendererProperties.storeToXML(new FileOutputStream(f2), "Test as xml");
         */


    }
}
