/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.bundler;

import com.vanamco.huvis.api.bundlerapi.BundlerProperties;
import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import com.vanamco.huvis.modules.toolbox.statusBar.StatusBarHandler;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * Provides the external interface to the Bundler process. It is
 * used by {@link BundlerServiceImpl} to achieve its task while enabling
 * maximum modularity and encapsulation.
 * @author ybrise
 */
public class BundlerExternal {

    private static File ant = null;
    private File antScript = null;
    private File bundlerBinFolder = null;
    private static final String KEY_ANT_EXEC = "ant";
    private static final String KEY_ANT_SCRIPT = "bundle.xml";
    private static final String KEY_BUNDLERBIN_FOLDER = "bundlerbin";
    private final BundlerServiceImpl bundlerService;
    private final FileObject workingDir;

    /**
     * 
     * Constructor for the Bundler process.
     * @param bundlerService A reference to the project's {@link com.vanamco.huvis.api.bundlerapi.BundlerService}.
     * @param workingDir    The working directory of the Bundler process to be executed.
     * @param clean         Indicates if the working directory should be cleaned while setting up the Bundler process.
     */
    public BundlerExternal(BundlerServiceImpl bundlerService, FileObject workingDir, boolean clean) {

        // get the executables and files packaged with the module
        antScript = InstalledFileLocator.getDefault().locate(
                "bundler" + PCTSettings.FILESEPARATOR + KEY_ANT_SCRIPT,
                "com.vanamco.huvis.modules.bundler",
                false);
        bundlerBinFolder = InstalledFileLocator.getDefault().locate(
                "bundler" + PCTSettings.FILESEPARATOR + KEY_BUNDLERBIN_FOLDER,
                "com.vanamco.huvis.modules.bundler",
                false);

        this.bundlerService = bundlerService;
        this.workingDir = workingDir;

        // clean if necessary
        if (clean) {
            cleanWorkingDirectory(workingDir);
        }
        // setup the executables and scripts
        setupWorkingDirectory(workingDir);
    }

    /**
     * This method runs the external bundler ant script.
     *
     * @param scenePictures Pointers to the images to be handled by bundler.
     */
    public boolean bundle(FileObject[] scenePictures) throws IOException {
        
        boolean success = true;
        
        
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Tried to run bundler from the "
                    + "event thread");
        }
        try {
            copyImageFilesToWorkingDir(scenePictures);
        } catch (IOException ex) {
            Logger.getLogger(BundlerExternal.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Get the POV-Ray executable
        ant = getAnt();
        if (ant == null) {
            //The user cancelled the file chooser w/o selecting
            StatusBarHandler.showMsg(NbBundle.getMessage(BundlerExternal.class, "MSG_NoAntExecutable"));
            return false;
        }

        String cmdline = ant.getPath() + " -f " + KEY_ANT_SCRIPT;


        StatusBarHandler.showMsg(NbBundle.getMessage(BundlerExternal.class, "MSG_Bundling", workingDir.getParent().getPath()));

        //Get an InputOutput to write to the output window
        InputOutput io_wf = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_WORKFLOW, false);
        InputOutput io = IOProvider.getDefault().getIO(PCTSettings.OUTPUT_REGISTER_BUNDLER, false);

        //Print the command line we're calling for debug purposes
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Bundler] reconstructing 3D scene from input pictures");
        io_wf.getOut().print(PCTSettings.timestamp());
        io_wf.getOut().println(" [Bundler] " + cmdline);
        io.select();
        io.getOut().println("cd " + workingDir.getPath());
        io.getOut().println(cmdline);

        // actually change to working directory
        System.setProperty("user.dir", workingDir.getPath());

        final Process process = Runtime.getRuntime().exec(cmdline, null, new File(workingDir.getPath()));

        //Get the standard out of the process
        InputStream out = new BufferedInputStream(process.getInputStream(), 8192);

        //Get the standard in of the process
        InputStream err = new BufferedInputStream(process.getErrorStream(), 8192);

        //Create readers for each
        final Reader outReader = new BufferedReader(new InputStreamReader(out));
        final Reader errReader = new BufferedReader(new InputStreamReader(err));

        //Create runnables to poll each output stream
        OutHandler processSystemOut = new OutHandler(outReader, io.getOut());
        OutHandler processSystemErr = new OutHandler(errReader, io.getErr());

        //Get two different threads listening on the output & err
        //using the system-wide thread pool
        RequestProcessor processor = new RequestProcessor("Output Processor", 100, false);
        processor.post(processSystemOut);
        processor.post(processSystemErr);

        try {
            synchronized (this) {
                //Hang this thread until the process exits
                process.waitFor();
                //System.out.println("Exit Value: " + process.exitValue());
                wait(2000); // HACK... we just wait for two seconds because with the previous waitFor there are sometimes still concurrency problems
                            // this is probably due to file I/O being slower than the actual shell process of the bundler.
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        

        //Close the output window's streams (title will become non-bold)
        processSystemOut.close();
        processSystemErr.close();


        // read out the settings/properties for storage in BundlerSettings,
        // and ultimately for use in the renderer
        
        // PRE: Bundler must have exited successfully, i.e. the list of jpgs
        //      and the list of focal distances must exist.

        BundlerProperties bundlerProperties = bundlerService.getProperties();

        FileObject outputDir = workingDir.getFileObject(BundlerProperties.OUTPUT_DIRECTORY_NAME);
        if (outputDir != null) {
            FileObject fileListJpgs = outputDir.getFileObject(BundlerProperties.OUTPUT_LIST_JPGS);
            if (fileListJpgs != null) {
                BufferedReader listJpgs = new BufferedReader(new FileReader(FileUtil.toFile(fileListJpgs)));

                String jpgFileName = listJpgs.readLine();

                // get an arbitrary image and read dimensions
                Image bimg = ImageIO.read(new File(jpgFileName));
                bundlerProperties.pushWidth(bimg.getWidth(null));
                bundlerProperties.pushHeight(bimg.getHeight(null));
                bundlerProperties.pushImageProportion(new Float(bundlerProperties.getHeight()) / bundlerProperties.getWidth());
            } else {
                success = false;
            }

            // compute focal length / opening angle
            FileObject fileListFocal = outputDir.getFileObject(BundlerProperties.OUTPUT_LIST_FOCAL);
            if (fileListFocal != null) {
                BufferedReader listFocal = new BufferedReader(new FileReader(FileUtil.toFile(fileListFocal)));
                String focalPixels = listFocal.readLine().split(" ")[2];
                // and so forth... also consider getting the bundler-computed focalLength
            } else {
                success = false;
            }
        } else {
            success = false;
        }

        /*
         if (outFile.exists() && process.exitValue() == 0) {
         //Try to find the new image file
         FileObject outFileObject = FileUtil.toFileObject(outFile);
         StatusBarHandler.showMsg(NbBundle.getMessage(PovrayExternal.class, "MSG_Success",
         outFile.getPath()));
         return outFileObject;
         } else {
         StatusBarHandler.showMsg(NbBundle.getMessage(PovrayExternal.class, "MSG_Failure",
         scene.getPath()));
         return null;
         }
         */
        io_wf.select();
        io_wf.getOut().print(PCTSettings.timestamp());
        if (success) {
            io_wf.getOut().println(" [Bundler] reconstruction complete");
        } else {
            io_wf.getOut().println(" [Bundler] recunstruction failed (hint: try deleting the bundler folder)");
        }
        
        return success;
    }

    // do cleaning with visitor pattern as well
    private void cleanWorkingDirectory(FileObject workingDir) {
        // safeguard against cleaning directories that are not
        // named "bundler".
        if (workingDir.getPath().endsWith("bundler")) {
            FileObject[] contents = workingDir.getChildren();
            for (FileObject file : contents) {
                if (!file.getPath().endsWith(KEY_BUNDLERBIN_FOLDER)
                        && !file.getPath().endsWith(KEY_ANT_SCRIPT)) {
                    try {
                        file.delete();
                    } catch (IOException ex) {
                        Logger.getLogger(BundlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

    }

    private void setupWorkingDirectory(FileObject workingDir) {

        // copy bundle.xml if it doesn't exist
        File testAntScript = new File(workingDir.getPath() + PCTSettings.FILESEPARATOR + KEY_ANT_SCRIPT);
        if (!testAntScript.exists()) {
            Path source = FileSystems.getDefault().getPath(antScript.getAbsolutePath());
            Path newdir = FileSystems.getDefault().getPath(workingDir.getPath());
            try {
                Files.copy(source, newdir.resolve(source.getFileName()));
                File file = newdir.resolve(source.getFileName()).toFile();
                file.setExecutable(true);
                file.setWritable(true);
                file.setReadable(true);
            } catch (IOException ex) {
                Logger.getLogger(BundlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // copy bundlerbin if it doesn't exist
        File testBundlerbin = new File(workingDir.getPath() + PCTSettings.FILESEPARATOR + KEY_BUNDLERBIN_FOLDER);
        if (!testBundlerbin.exists()) {
            Path source = FileSystems.getDefault().getPath(bundlerBinFolder.getAbsolutePath());
            Path newdir = FileSystems.getDefault().getPath(workingDir.getPath() + PCTSettings.FILESEPARATOR + KEY_BUNDLERBIN_FOLDER);
            validate(source);
            try {
                Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new CopyDirVisitor(source, newdir));
            } catch (IOException ex) {
                Logger.getLogger(BundlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // TODO: add functionality to scale pics
    private void copyImageFilesToWorkingDir(FileObject[] scenePictures) throws IOException {
        Path newdir = FileSystems.getDefault().getPath(workingDir.getPath());
        if (scenePictures != null) {
            for (FileObject image : scenePictures) {
                Path imagePath = FileSystems.getDefault().getPath(image.getPath());
                Files.copy(imagePath, newdir.resolve(imagePath.getFileName()));
            }
        }
    }

    private static class CopyDirVisitor extends SimpleFileVisitor<Path> {

        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;

        public CopyDirVisitor(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        public CopyDirVisitor(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.COPY_ATTRIBUTES);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            targetPath.toFile().setReadable(true);
            targetPath.toFile().setWritable(true);
            targetPath.toFile().setExecutable(true);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            Files.copy(path, toPath.resolve(fromPath.relativize(path)), copyOption);
            File file = path.toFile();
            file.setReadable(true);
            file.setWritable(true);
            file.setExecutable(true);
            return FileVisitResult.CONTINUE;
        }
    }

    private static void validate(Path... paths) {
        for (Path path : paths) {
            Objects.requireNonNull(path);
            if (!Files.isDirectory(path)) {
                throw new IllegalArgumentException(String.format("%s is not a directory", path.toString()));
            }
        }
    }

    @NbBundle.Messages({"TTL_FindAnt=Locate ant Executable",
        "MSG_WindowsWarning=No idea about windows"
    })
    private static File getAnt() {
        if (ant == null || !ant.exists()) {
            Preferences prefs = BundlerServiceImpl.getPreferences();
            String loc = prefs.get(KEY_ANT_EXEC, null);
            if (loc != null) {
                ant = new File(loc);
            }
            if (ant == null || !ant.exists()) {
                File maybeAnt = locate("TTL_FindAnt");
                if (maybeAnt != null && maybeAnt.getPath().endsWith("ant.exe")) {
                    //Warn the user to get a command line build:
                    NotifyDescriptor msg = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(BundlerServiceImpl.class,
                            Bundle.MSG_WindowsWarning()),
                            NotifyDescriptor.WARNING_MESSAGE);
                    Object result = DialogDisplayer.getDefault().notify(msg);
                    if (result == NotifyDescriptor.CANCEL_OPTION) {
                        return null;
                    }
                }
                ant = maybeAnt;
                if (ant != null) {
                    prefs.put(KEY_ANT_EXEC, ant.getPath());
                }
            }
        }
        return ant;
    }

    private static File locate(String key) {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle(NbBundle.getMessage(BundlerExternal.class, key));
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // TODO: There's a pitfall here. It would be better to anchor the file
        // chooser dialog to the main window, so it always stays on top. However,
        // we are potentially (most probably) calling this method from a different
        // thread, i.e., the rendering thread. This is not allowed and results in: 
        // "java.lang.IllegalStateException: Problem in some module which uses
        // Window System: Window System API is required to be called from AWT
        // thread only, see http://core.netbeans.org/proposals/threading/"
        // Therefore, we should think of a better place of doing this, e.g., handling
        // the intallation of the external executables before calling render. In the
        // preferences, upon installation, upon first execution, etc...
        //jfc.showOpenDialog(WindowManager.getDefault().getMainWindow());
        jfc.showOpenDialog(null);
        File result = jfc.getSelectedFile();
        return result;
    }

    static class OutHandler implements Runnable {

        private Reader out;
        private OutputWriter writer;

        public OutHandler(Reader out, OutputWriter writer) {
            this.out = out;
            this.writer = writer;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    while (!out.ready()) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            close();
                            return;
                        }
                    }
                    if (!readOneBuffer() || Thread.currentThread().isInterrupted()) {
                        close();
                        return;
                    }
                } catch (IOException ioe) {
                    //Stream already closed, this is fine
                    return;
                }
            }
        }

        private boolean readOneBuffer() throws IOException {
            char[] cbuf = new char[255];
            int read;
            while ((read = out.read(cbuf)) != -1) {
                writer.write(cbuf, 0, read);
            }
            return read != -1;
        }

        private void close() {
            try {
                out.close();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                writer.close();
            }
        }
    }
}
