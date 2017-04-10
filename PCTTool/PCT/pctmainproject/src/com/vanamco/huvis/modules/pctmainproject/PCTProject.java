/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.api.bundlerapi.BundlerService;
import com.vanamco.huvis.api.povray.RenderProperties;
import com.vanamco.huvis.api.povray.RendererService;
import com.vanamco.huvis.modules.bundler.BundlerServiceImpl;
import com.vanamco.huvis.modules.povray.RendererServiceImpl;
import com.vanamco.huvis.modules.toolbox.projectRelated.NotifyProperties;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ybrise
 */
public final class PCTProject implements Project {

    private final FileObject projectDir;
    /**
     *
     */
    protected LogicalViewProvider logicalView = new PCTLogicalView(this);
    private final PCTProjectState state;
    private Thread stateThread;
    private NotifyProperties mainProperties;
    /**
     *
     */
    public BundlerProperties bundlerProperties;
    /**
     *
     */
    public RenderProperties renderProperties;
    private Lookup lkp;
    

    public enum NodeColor {

        /**
         *
         */
        GREEN,
        /**
         *
         */
        YELLOW,
        /**
         *
         */
        ORANGE,
        /**
         *
         */
        RED
    };

    public PCTProject(FileObject projectDir, ProjectState state) {
        this.projectDir = projectDir;
        this.state = new PCTProjectState(state, this);
        this.bundlerProperties = new BundlerProperties(state, this.state); // state needs to be constructed!
        this.renderProperties = new RenderProperties(state, this.state); // state needs to be constructed!
        this.mainProperties = loadProperties(); // state needs to be constructed!
        this.state.postInitState(); // post-initialize state (mainly properties)
        stateThread = new Thread(this.state); // initialize state thread (for continuous state tasks)
    }
    
    void postInitProject() {
            state.markModified(); // force saving of the properties file; this is
                              // necessary because at construction time, it's not
                              // allowed to mark the project state as modified
                              // by the PCTProjectFactory.
            state.myNotify();     // force redrawing of all nodes at the beginning
    }

    /**
     * This method is used to start the sate thread. I.e. to let the project
     * start watching for filesystem changes. It is important this is not called
     * during the loading phase (which includes construction of course) of the
     * project, in particular the loadProject method of the ProjectFactory. It
     * is important that this IS called however, because otherwise we are not
     * watching for filesystem changes.
     */
    public void startStateThread() {
        if (!stateThread.isAlive()) {
            stateThread.start();
        }
    }

    /**
     * This method is used to internally consolidate the project settings, e.g.,
     * if the properties between Bundler and render process need to be
     * synchronized.
     */
    public void consolidate() {
        // translate width & height from bundler properties to render properties
        // TODO: Why can't we do this on the members this.bundlerProperties and
        //       this.renderProperties directly? For some reason this is not
        //       working.
        getLookup().lookup(RendererService.class).getProperties().setWidth(getLookup().lookup(BundlerService.class).getProperties().getWidth());
        getLookup().lookup(RendererService.class).getProperties().setHeight(getLookup().lookup(BundlerService.class).getProperties().getHeight());

    }

    /**
     *
     * @return
     */
    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    /**
     *
     * @param create
     * @return
     */
    public FileObject getBundlerFolder(boolean create) {
        FileObject result = projectDir.getFileObject(PCTSettings.DIR_BUNDLER);

        if (result == null && create) {
            try {
                result = projectDir.createFolder(PCTSettings.DIR_BUNDLER);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    /**
     *
     * @param create
     * @return
     */
    public FileObject getPovrayFolder(boolean create) {
        FileObject result =
                projectDir.getFileObject(PCTSettings.DIR_POVRAY);

        if (result == null && create) {
            try {
                result = projectDir.createFolder(PCTSettings.DIR_POVRAY);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }
    
    /**
     *
     * @param folder
     */
    public void emptyFolder(FileObject folder) {
        FileObject[] children = folder.getChildren();
        for (FileObject file: children) {
            try {
                file.delete();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     *
     * @param create
     * @return
     */
    public FileObject getInputFolder(boolean create) {
        FileObject result =
                projectDir.getFileObject(PCTSettings.DIR_INPUT);

        if (result == null && create) {
            try {
                result = projectDir.createFolder(PCTSettings.DIR_INPUT);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    /**
     *
     * @param create
     * @return
     */
    public FileObject getOutputFolder(boolean create) {
        FileObject result =
                projectDir.getFileObject(PCTSettings.DIR_OUTPUT);

        if (result == null && create) {
            try {
                result = projectDir.createFolder(PCTSettings.DIR_OUTPUT);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    private NotifyProperties loadProperties() {
        FileObject fob = projectDir.getFileObject(PCTSettings.DIR_PROJECT
                + PCTSettings.FILESEPARATOR + PCTSettings.PROPFILE_PROJECT);

        NotifyProperties props = new NotifyProperties(state);
        if (fob != null) {
            try {
                props.load(fob.getInputStream());
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
        return props;
    }

    // TODO: possibly only a selection of the input pictures...
    // TODO: possibly unify file handling with PCTCustomFileFilter (swing filter)
    // and/or functionality in BundlerExternal (walkFileTree stuff)
    /**
     *
     * @return
     */
    public FileObject[] getInputPictures() {

        List<FileObject> scenePictures = new ArrayList<FileObject>();

        FileObject[] inputContents = getInputFolder(true).getChildren();
        for (FileObject file : inputContents) {
            if (file.getExt().toLowerCase().equals(PCTSettings.EXTENSION_INPUT_TYPE)) {
                scenePictures.add(file);
            }
        }
        return scenePictures.toArray(new FileObject[scenePictures.size()]);
    }
    
    /**
     *
     * @return
     */
    public boolean hasInputPictures() {
        return getInputPictures().length > 0;
    }

    /**
     *
     * @return
     */
    public FileObject[] getRenderedPictures() {
        List<FileObject> renderedPictures = new ArrayList<FileObject>();

        FileObject[] povrayContents = getPovrayFolder(true).getChildren();
        for (FileObject file : povrayContents) {
            if (file.getExt().toLowerCase().equals(PCTSettings.EXTENSION_RENDER_TYPE)) {
                renderedPictures.add(file);
            }
        }

        return renderedPictures.toArray(new FileObject[renderedPictures.size()]);
    }

    // TODO: possibly only a selection of the include files...
    // TODO: possibly unify file handling with PCTCustomFileFilter (swing filter)
    // and/or functionality in BundlerExternal (walkFileTree stuff)
    /**
     *
     * @return
     */
    public FileObject[] getPovrayIncludeFiles() {

        List<FileObject> includeFiles = new ArrayList<FileObject>();

        FileObject[] inputContents = getInputFolder(true).getChildren();
        for (FileObject file : inputContents) {
            if (file.getExt().toLowerCase().equals(PCTSettings.EXTENSION_POVRAY_INCLUDE_TYPE)) {
                includeFiles.add(file);
            }
        }
        return includeFiles.toArray(new FileObject[includeFiles.size()]);
    }

    /**
     *
     * @return
     */
    public FileObject getTransformationFile() {
        FileObject ret = null;
        FileObject inputDir = getInputFolder(true);

        if (inputDir != null) {
            ret = inputDir.getFileObject(PCTSettings.TRANSFORMATION_FILE_NAME);
        }

        return ret;
    }
    
    /**
     *
     * @return
     */
    public FileObject getBundlerOutputDir() {
        FileObject ret = null;
        FileObject workingDir = getBundlerFolder(true);
        if (workingDir != null) {
            ret = workingDir.getFileObject(BundlerProperties.OUTPUT_DIRECTORY_NAME);
        }
        return ret;
    }
    
    /**
     *
     * @return
     */
    public FileObject getBundlerOutputFile() {
        FileObject ret = null;
        FileObject workingDir = getBundlerFolder(true);
        if (workingDir != null) {
            FileObject bundlerOutputDir = getBundlerOutputDir();
            if (bundlerOutputDir != null) {
                ret = bundlerOutputDir.getFileObject(BundlerProperties.BUNDLE_FILE_NAME);
            }
        }
        return ret;
    }
    
    /**
     *
     * @return
     */
    public String[] getBundlerJpgList() {
        List<String> scenePicturesPaths = new ArrayList<String>();

        FileObject bundlerOutputDir = getBundlerOutputDir();
        if (bundlerOutputDir != null) {
            FileObject jpgListFileObject = bundlerOutputDir.getFileObject(BundlerProperties.OUTPUT_LIST_JPGS);
            if (jpgListFileObject != null) {
                try {
                    BufferedReader jpgListBufferedReader = new BufferedReader(new FileReader(FileUtil.toFile(jpgListFileObject)));
                    if (jpgListBufferedReader != null) {
                        String line;
                        while ((line = jpgListBufferedReader.readLine()) != null && !line.equals("")) {
                            scenePicturesPaths.add(line.trim());
                        }
                    }
                } catch (FileNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return scenePicturesPaths.toArray(new String[scenePicturesPaths.size()]);
    }
    
    /**
     *
     * @return
     */
    public boolean isReconstructionCurrent() {
        boolean ret = true;

        // check if output file exsits
        FileObject outputFile = getBundlerOutputFile();
        if (outputFile == null) {
            ret = false;
        } else {

            // check if lists of files correspond
            String[] listOfPaths = getBundlerJpgList();
            FileObject[] listOfFiles = getInputPictures();
            for (String filePath : listOfPaths) {
                boolean found = false;
                for (FileObject file : listOfFiles) {
                    if (file.getNameExt().equals(filePath.substring(filePath.lastIndexOf(PCTSettings.FILESEPARATOR)+1))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ret = false;
                    break;
                }
            }
        }
        // check
        return ret;
    }
    

    private final class ActionProviderImpl implements ActionProvider {

        @Override
        public String[] getSupportedActions() {
            return new String[]{
                        ActionProvider.COMMAND_BUILD,
                        ActionProvider.COMMAND_CLEAN
                    };
        }

        @Override
        public void invokeAction(String string, Lookup lookup) throws IllegalArgumentException {
            int idx = Arrays.asList(getSupportedActions()).indexOf(string);
            final PCTProject project = getLookup().lookup(PCTProject.class);
            final Info projectInfo = getLookup().lookup(Info.class);
            InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
            switch (idx) {
                case 0: //build
                    

                    // prepare dedicated output
                    io_wf.select();
                    io_wf.getOut().println();
                    io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                    io_wf.getOut().println("*\tBuilding project " + projectInfo.getName());
                    io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);

                    final BundlerService bundler = getLookup().lookup(BundlerService.class);
                    RequestProcessor processorBuild = new RequestProcessor("Build Task", 1, true);
                    // TODO: double-threading... new Runnable() {run(){...run()}}. Not sure if this is a good way to do it.
                    processorBuild.post(new Runnable() {
                        @Override
                        public void run() {
                            
                            if (state.progress < PCTProjectState.PCT_PROJECT_PROGRESS_BUNDLED) {
                                PCTActionBundler bundlerAction = new PCTActionBundler(project);
                                bundlerAction.run();
                            }
                            
                            
                            // do transformation in any case
                            PCTActionTransformation transformationAction = new PCTActionTransformation(project);
                            transformationAction.run();
                            
                            if (state.progress >= PCTProjectState.PCT_PROJECT_PROGRESS_BUNDLED_OUTDATED && state.progress < PCTProjectState.PCT_PROJECT_PROGRESS_POVRAYED) {
                                PCTActionGeneratePovrayScripts povrayAction = new PCTActionGeneratePovrayScripts(project);
                                povrayAction.run();
                            }
                            
                            // total hack... we have to wait until the generated povray files are noticed by the system
                            synchronized(this) {
                                try {
                                    wait(3000);
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            
                            if (state.progress >= PCTProjectState.PCT_PROJECT_PROGRESS_POVRAYED && state.progress < PCTProjectState.PCT_PROJECT_PROGRESS_RENDERED_FULL) {
                                PCTActionRenderAll renderAction = new PCTActionRenderAll(project);
                                renderAction.run();
                            }
                            
                            
                            // wrapping up dedicated output (careful: we need new copy of io_wf because we're inside a runnable)
                            InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
                            io_wf.select();
                            io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                            io_wf.getOut().println("*\t Finished building project " + projectInfo.getName());
                            io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                            io_wf.getOut().println();
                        }
                    });
                    break;
                case 1: //clean
                    // prepare dedicated output
                    io_wf.select();
                    io_wf.getOut().println();
                    io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                    io_wf.getOut().println("*\tCleaning project " + projectInfo.getName());
                    io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                    io_wf.getOut().print(PCTSettings.timestamp());
                    io_wf.getOut().println(" [PCT] remove attached transformation if applicable");
                    RequestProcessor processorClean = new RequestProcessor("Clean Task", 1, true);
                    
                    // reset bundler and transformation
                    processorClean.post(new PCTActionRemoveTransformation(project));
                    BundlerService bundlerService = project.getLookup().lookup(BundlerService.class);
                    bundlerService.reset();

                    
                    // empty folders
                    emptyFolder(getPovrayFolder(true));
                    emptyFolder(getOutputFolder(true));
                    emptyFolder(getBundlerFolder(true));
                                        
                    // reset state
                    state.pushProgress(PCTProjectState.PCT_PROJECT_PROGRESS_CLEAN);
                    
                    // recreate folders
                    project.getOutputFolder(true); // dummy call to create folder
                    project.getPovrayFolder(true); // dummy call to create folder
                    
                    // refresh view (only experimenting)
                    /*
                    Mode mode = WindowManager.getDefault().findMode("explorer");
                    if (mode != null) {
                        System.out.println(mode.getName());
                    } else {
                        System.out.println("gugugug");
                    }*/
                    /*for (TopComponent component: topComponents) {
                        System.out.println(component.getName());
                    }*/
                    
                    // wrapping up dedicated output
                    io_wf.select();
                    io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                    io_wf.getOut().println("*\t Finished cleaning project " + projectInfo.getName());
                    io_wf.getOut().println(PCTSettings.OUTPUT_SEPARATOR);
                    io_wf.getOut().println();
                    
                    break;
                default:
                    throw new IllegalArgumentException(string);
            }
        }

        @Override
        public boolean isActionEnabled(String string, Lookup lookup) throws IllegalArgumentException {
            int idx = Arrays.asList(getSupportedActions()).indexOf(string);
            boolean result;
            switch (idx) {
                case 0: //build
                    result = true;
                    break;
                case 1: //clean
                    result = true;
                    break;
                default:
                    result = false;
            }
            return result;
        }
    }

    /**
     * Implementation of project system's ProjectInformation class
     */
    private final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(
                    "com/vanamco/huvis/modules/pctmainproject/resources/pct_project.png"));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return PCTProject.this;
        }
    }

    /**
     * Implementation of project's lookup
     */
    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{
                        this, //handy to expose a project in its own lookup
                        state, //allow outside code to mark the project as needing saving
                        new ActionProviderImpl(), //Provides standard actions like Build and Clean
                        mainProperties, //The project properties
                        new Info(), //Project information implementation
                        logicalView, //Logical view of project implementation
                        new BundlerServiceImpl(bundlerProperties), // Bundler service implementation
                        new RendererServiceImpl(renderProperties), // RendererService implementation
                    });
        }
        return lkp;
    }

    /**
     * Implementation of the project's state; can be obtained through the
     * projects lookup, but cannot and should not be independently instantiated.
     * The project and the state contain links to each other, hence the
     * restricted creation pattern.
     */
    public static class PCTProjectState extends Observable implements ProjectState, Runnable, PropertyChangeListener {

        private final ProjectState delegateState;
        private final PCTProject parentProject;
        
        /**
         * Project states. Integer values should be unique and increasing.
         */
        public static final int PCT_PROJECT_PROGRESS_INVALID = 0;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_UNDEFINED = 1;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_NO_INPUT = 2;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_CLEAN = 3;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_DIRTY = 4;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_BUNDLED_OUTDATED = 5;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_BUNDLED = 6;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_POVRAYED = 7;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_RENDERED_OUTDATED = 8;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_RENDERED_PARTIAL = 9;
        /**
         *
         */
        public static final int PCT_PROJECT_PROGRESS_RENDERED_FULL = 10;
        
        
        private int progress = PCT_PROJECT_PROGRESS_UNDEFINED;
        
        private NodeColor nodeColorTransformations = NodeColor.RED;
        private NodeColor nodeColorInput = NodeColor.RED;
        private NodeColor nodeColorReconstruction =  NodeColor.RED;
        private NodeColor nodeColorPovray = NodeColor.RED;
        private NodeColor nodeColorVisualization = NodeColor.RED;

        // THIS CONSTRUCTOR SHOULD NOT BE CALLED!
        // purpose: prevent construction outside the scope of the project
        private PCTProjectState() {
            throw new UnsupportedOperationException("this private constructor is present to prevent construction outside the project class");
        }

        private PCTProjectState(ProjectState state, PCTProject parentProject) {
            this.delegateState = state;
            this.parentProject = parentProject;
            this.progress = PCT_PROJECT_PROGRESS_CLEAN;
            // is it really clean? possibly make it so...
        }
        
        /**
         * This method initializes the values from the property file. This
         * is necessary because of the mutual dependence between state and 
         * properties.
         */
        private void postInitState() {
            String tmpProgressStr = parentProject.mainProperties.getProperty("pctProjectProgress");
            if (tmpProgressStr != null) {
                setProgress(Integer.parseInt(tmpProgressStr));
                // check whether it's true...
            } else {
                setProgress(PCT_PROJECT_PROGRESS_UNDEFINED);
            }
            parentProject.mainProperties.setProperty("pctProjectProgress", new Integer(this.progress).toString());

        }

        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().startsWith("renderer-") && evt.getNewValue() != evt.getOldValue() && progress > PCT_PROJECT_PROGRESS_POVRAYED) {
                pushProgress(PCT_PROJECT_PROGRESS_POVRAYED);
            } else if (evt.getPropertyName().startsWith("bundler-featuresize") && evt.getNewValue() != evt.getOldValue() && progress > PCT_PROJECT_PROGRESS_BUNDLED) {
                pushProgress(PCT_PROJECT_PROGRESS_BUNDLED);
            }
            //System.out.println(evt);
        }
        
        
        /**
         *
         */
        @Override
        public void markModified() {
            delegateState.markModified();
            myNotify();
        }

        /**
         *
         * @throws IllegalStateException
         */
        @Override
        public void notifyDeleted() throws IllegalStateException {
            delegateState.notifyDeleted();
        }

        /**
         *
         * @return
         */
        public int getProgress() {
            return progress;
        }
        
        /**
         *
         * @param progress
         */
        public void setProgress(int progress) {
            
            // determine visualization node color
            if (progress >= PCT_PROJECT_PROGRESS_RENDERED_FULL) {
                nodeColorVisualization = NodeColor.GREEN;
            } else if (progress >= PCT_PROJECT_PROGRESS_RENDERED_PARTIAL) {
                nodeColorVisualization = NodeColor.YELLOW;
            } else if (progress == PCT_PROJECT_PROGRESS_RENDERED_OUTDATED) {
                nodeColorVisualization = NodeColor.ORANGE;
            } else if (progress >= PCT_PROJECT_PROGRESS_POVRAYED) {
                nodeColorVisualization = NodeColor.YELLOW;
            } else {
                nodeColorVisualization = NodeColor.RED;
            }
            
            // determine povray node color
            if (progress >= PCT_PROJECT_PROGRESS_POVRAYED) {
                nodeColorPovray = NodeColor.GREEN;
            } else if (progress >= PCT_PROJECT_PROGRESS_BUNDLED_OUTDATED) {
                nodeColorPovray = NodeColor.YELLOW;
            } else {
                nodeColorPovray = NodeColor.RED;
            }
                
            // determine reconstruction node color    
            if (progress >= PCT_PROJECT_PROGRESS_BUNDLED) {
                nodeColorReconstruction = NodeColor.GREEN;
            } else if (progress >= PCT_PROJECT_PROGRESS_BUNDLED_OUTDATED) {
                nodeColorReconstruction = NodeColor.ORANGE;
            } else if (parentProject.hasInputPictures()) {
                nodeColorReconstruction = NodeColor.YELLOW;
            } else {
                nodeColorReconstruction = NodeColor.RED;
            }
            this.progress = progress;
            parentProject.mainProperties.setProperty("pctProjectProgress", new Integer(progress).toString());
        }

        /**
         *
         * @param progress
         */
        public void pushProgress(int progress) {
            setProgress(progress);
            myNotify();
        }

        /**
         *
         */
        public void confirmTransformation() {
            if (!Boolean.parseBoolean(parentProject.mainProperties.getProperty("hasTransformationAttached"))) {
                parentProject.mainProperties.pushProperty("hasTransformationAttached", "true");
                nodeColorTransformations = NodeColor.GREEN;
                if (progress > PCT_PROJECT_PROGRESS_BUNDLED) {
                    setProgress(PCT_PROJECT_PROGRESS_BUNDLED);
                }
                myNotify();
            }
        }

        /**
         *
         */
        public void removeTransformation() {
            parentProject.mainProperties.pushProperty("hasTransformationAttached", "false");
            FileObject transformationFileObject = parentProject.getTransformationFile();
            if (transformationFileObject != null) {
                nodeColorTransformations = NodeColor.YELLOW;
            } else {
                nodeColorTransformations = NodeColor.RED;
            }
            if (progress > PCT_PROJECT_PROGRESS_BUNDLED) {
                setProgress(PCT_PROJECT_PROGRESS_BUNDLED);
            }
            myNotify();
        }

        /**
         *
         * @return
         */
        public boolean hasTransformationAttached() {
            return Boolean.parseBoolean(parentProject.mainProperties.getProperty("hasTransformationAttached"));
        }

        /**
         *
         * @return
         */
        public synchronized NodeColor getNodeColorTransformation() {
            return nodeColorTransformations;
        }
        
        /**
         *
         * @return
         */
        public synchronized NodeColor getNodeColorInput() {
            return nodeColorInput;
        }
        
        /**
         *
         * @return
         */
        public synchronized NodeColor getNodeColorReconstruction() {
            return nodeColorReconstruction;
        }
        
        /**
         *
         * @return
         */
        public synchronized NodeColor getNodeColorPovray() {
            return nodeColorPovray;
        }
        
        /**
         *
         * @return
         */
        public synchronized NodeColor getNodeColorVisualization() {
            return nodeColorVisualization;
        }

        private void myNotify() {
            this.setChanged();
            this.notifyObservers();
            this.clearChanged();
        }
        
        
        @Override
        public void run() {
            try {

                // get output device
                InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);

                //markModified(); // Force saving at closing time
                
                // initialize input node color
                nodeColorInput = (parentProject.hasInputPictures() ? NodeColor.GREEN : NodeColor.RED);

                // check state of transformation
                FileObject transformationFileObject = parentProject.getTransformationFile();
                if (transformationFileObject != null) {
                    if (hasTransformationAttached()) {
                        // actually load transformation
                        PCTActionTransformation actionTransformation = new PCTActionTransformation(parentProject);
                        RequestProcessor processor = new RequestProcessor("Transformation Action", 1, true);
                        processor.post(actionTransformation);
                        nodeColorTransformations = NodeColor.GREEN;
                    } else {
                        // transformation available, but should not be loaded
                        nodeColorTransformations = NodeColor.YELLOW;
                    }
                } else {
                    nodeColorTransformations = NodeColor.RED;
                    parentProject.mainProperties.pushProperty("hasTransformationAttached", "false");
                }

                // after all the intial checking, let's notify all observers
                myNotify();

                // check directories
                //create the watchService
                final WatchService watchService = FileSystems.getDefault().newWatchService();

                //register the directory with the watchService
                //for create, modify and delete events
                final Path path = Paths.get(parentProject.getInputFolder(true).toURI());


                path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);

                //start an infinite loop
                while (true) {

                    //remove the next watch key
                    final WatchKey key = watchService.take();

                    //get list of events for the watch key
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {

                        //get the filename for the event
                        final WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
                        final Path contextPath = ev.context();

                        if (contextPath.toString().equals(PCTSettings.TRANSFORMATION_FILE_NAME)) {
                            //get the kind of event (create, modify, delete)
                            final WatchEvent.Kind<?> kind = watchEvent.kind();
                            System.out.println(kind + ": " + contextPath);

                            // PRE: hasTransformationAttached corresponds to an actual transformation being loaded (not just presence of file)
                            synchronized (this) {
                                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                    if (!hasTransformationAttached() && nodeColorTransformations != NodeColor.ORANGE) {
                                        nodeColorTransformations = NodeColor.YELLOW;
                                    } else {
                                        nodeColorTransformations = NodeColor.ORANGE;
                                        parentProject.mainProperties.pushProperty("hasTransformationAttached", "false");
                                        io_wf.getOut().print(PCTSettings.timestamp());
                                        io_wf.getOut().println(" [Transformation] new transformation file available; previous one is still loaded");
                                    }
                                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    nodeColorTransformations = NodeColor.ORANGE;
                                    parentProject.mainProperties.pushProperty("hasTransformationAttached", "false");
                                    io_wf.getOut().print(PCTSettings.timestamp());
                                    io_wf.getOut().println(" [Transformation] transformation file was modified; previous one is still loaded");
                                } else {
                                    if (!hasTransformationAttached()) {
                                        nodeColorTransformations = NodeColor.RED;
                                        io_wf.getOut().print(PCTSettings.timestamp());
                                        io_wf.getOut().println(" [Transformation] transformation file was deleted");
                                    } else {
                                        nodeColorTransformations = NodeColor.ORANGE;
                                        parentProject.mainProperties.pushProperty("hasTransformationAttached", "false");
                                        io_wf.getOut().print(PCTSettings.timestamp());
                                        io_wf.getOut().println(" [Transformation] transformation file was deleted; it is still loaded, but will not be saved again");
                                    }
                                }
                                markModified();
                            }
                        } else if (contextPath.getFileName().toString().toLowerCase().endsWith(new String("jpg").toLowerCase())) {
                            
                            if (parentProject.hasInputPictures()) {
                                // number of pictures has changed or pictures have been modifed
                                if (progress >= PCT_PROJECT_PROGRESS_BUNDLED) {
                                    pushProgress(PCT_PROJECT_PROGRESS_BUNDLED_OUTDATED);
                                } else {
                                    pushProgress(PCT_PROJECT_PROGRESS_DIRTY);
                                }
                            } else {
                                // no pictures any more
                                pushProgress(PCT_PROJECT_PROGRESS_NO_INPUT);
                            }
                            // check if there are any files at all
                            markModified(); // fire event regardless of whether the number has gone from or to zero
                        }

                    }

                    // reset the key
                    boolean valid = key.reset();

                    // exit loop if the key is not valid
                    // e.g. if the directory was deleted
                    if (!valid) {
                        break;
                    }
                }
            } catch (Exception e) {
                // nothing for now
            }
        }        
    }
}
