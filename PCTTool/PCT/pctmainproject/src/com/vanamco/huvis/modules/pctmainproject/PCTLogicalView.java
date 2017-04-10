/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vanamco.huvis.modules.pctmainproject;

import com.vanamco.huvis.modules.toolbox.projectRelated.PCTSettings;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.actions.FileSystemAction;
import org.openide.actions.NewTemplateAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Represents the logical view of a Point Cloud Tool project in the project
 * view.
 *
 * @author ybrise
 */
public class PCTLogicalView implements LogicalViewProvider {

    private final PCTProject project;

    public PCTLogicalView(PCTProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {



        try {

            //Get the scenes directory, creating if deleted:
            FileObject pct_input = project.getInputFolder(true);

            //Get the DataObject that represents it:
            DataFolder inputDataObject =
                    DataFolder.findFolder(pct_input);

            //Get its default node—we'll wrap our node around it to change the
            //display name, icon, etc:
            Node realInputFolderNode = inputDataObject.getNodeDelegate();

            //This FilterNode will be our project node:
            List<String> fileEndings = new ArrayList<String>();
            fileEndings.add("." + PCTSettings.EXTENSION_INPUT_TYPE);
            fileEndings.add("." + PCTSettings.EXTENSION_POVRAY_INCLUDE_TYPE);

            //return new PCTFilterNode(realInputFolderNode, project, fileEndings);
            return new PCTMainNode(realInputFolderNode, project);


        } catch (DataObjectNotFoundException donfe) {

            Exceptions.printStackTrace(donfe);

            //Fallback—the directory couldn't be created -
            //read-only filesystem or something evil happened:
            return new AbstractNode(Children.LEAF);

        }

    }

    private static final class PCTMainNode extends AbstractNode {

        final PCTProject project;

        public PCTMainNode(Node node, PCTProject project) throws DataObjectNotFoundException {
            //super(new FilterNode.Children(node),
            super(new Children.Array(),
                    //The projects system wants the project in the Node's lookup.
                    //NewAction and friends want the original Node's lookup.
                    //Make a merge of both:
                    new ProxyLookup(
                    Lookups.singleton(project),
                    node.getLookup()));


            // setup worklow nodes of the project
            Node[] childrenWorkflow;
            childrenWorkflow = new Node[5];
            childrenWorkflow[0] = new PCTNodeInput(project);
            childrenWorkflow[1] = new PCTNodeReconstruction(project);
            childrenWorkflow[2] = new PCTNodeTransformations(project);
            childrenWorkflow[3] = new PCTNodePovray(project);
            childrenWorkflow[4] = new PCTNodeVisualization(project);
            PCTProject.PCTProjectState projectState = project.getLookup().lookup(PCTProject.PCTProjectState.class);
            for (int i = 0; i < 5; ++i) {
                projectState.addObserver((Observer) childrenWorkflow[i]);
            }

            /*
             * TODO: there should be a more generic place to start the
             * project-state-thread. For now, we start it here. It is important
             * that this is NOT called in the constructor of the project,
             * because this will possibly violate an assertion in the
             * ProjectManager that the state must not be marked as modified.
             * EDIT1: In any case, maybe this isn't the worst place
             * after all, because only after the previous for loop, will the
             * observers actually be registered (and calls to notify have
             * an effect at all)
             * EDIT2: Maybe it is a bad place because the main node
             * may be constructed several times.
             * EDIT3: We've remedied the problem described in EDIT2, by letting
             * startStateThread only have an effect if the project-state-thread
             * is not yet alive. So... we leave the following call at this spot.
             */
            project.startStateThread();
            project.postInitProject();


            this.getChildren().add(childrenWorkflow);
            this.project = project;
        }

        @NbBundle.Messages({
            "PCT_Build=Build Project",
            "PCT_Clean=Clean Project"
        })
        @Override
        public Action[] getActions(boolean popup) {
            Action[] result = new Action[]{
                new ProjectAction(ActionProvider.COMMAND_BUILD, Bundle.PCT_Build(), project),
                new ProjectAction(ActionProvider.COMMAND_CLEAN, Bundle.PCT_Clean(), project),
                new OtherProjectAction(project, false),
                SystemAction.get(NewTemplateAction.class),
                SystemAction.get(FileSystemAction.class),
                new OtherProjectAction(project, true),};
            return result;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(
                    "com/vanamco/huvis/modules/pctmainproject/resources/pct_project.png");
        }

        @Override
        public String getDisplayName() {
            return project.getProjectDirectory().getName();
        }

        @Override
        public boolean canCopy() {
            return false;
        }

        @Override
        public boolean canCut() {
            return false;
        }

        @Override
        public boolean canDestroy() {
            return false;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        static class FileFilteredChildren extends FilterNode.Children {

            private final FileFilter fileFilter;

            public FileFilteredChildren(Node owner, FileFilter fileFilter) {
                super(owner);
                this.fileFilter = fileFilter;
            }

            /*
             @Override
             protected Node copyNode(Node original) {
             return new ScenesNode(original, fileFilter);
             }
             */
            @Override
            protected Node[] createNodes(Node object) {
                List<Node> result = new ArrayList<Node>();

                for (Node node : super.createNodes(object)) {
                    FileObject fileObject = node.getLookup().lookup(FileObject.class);
                    if (fileObject != null) {
                        File file = FileUtil.toFile(fileObject);
                        if (fileFilter.accept(file)) {
                            result.add(node);
                        }
                    }
                }

                return result.toArray(new Node[result.size()]);
            }
        }
    }

    private static class ProjectAction extends AbstractAction {

        private final PCTProject project;
        private final String command;

        public ProjectAction(String cmd, String displayName, PCTProject prj) {
            this.project = prj;
            putValue(NAME, displayName);
            this.command = cmd;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            ActionProvider prov = (ActionProvider) project.getLookup().lookup(ActionProvider.class);
            prov.invokeAction(command, null);
        }

        @Override
        public boolean isEnabled() {
            ActionProvider prov = (ActionProvider) project.getLookup().lookup(ActionProvider.class);
            return prov.isActionEnabled(command, null);
        }
    }

    @NbBundle.Messages({
        "PCT_CloseProject=Close Project",
        "PCT_SetMainProject=Set Main Project"
    })
    private static class OtherProjectAction extends AbstractAction {

        private final PCTProject project;
        private final boolean isClose;

        OtherProjectAction(PCTProject project, boolean isClose) {
            putValue(NAME, isClose ? Bundle.PCT_CloseProject() : Bundle.PCT_SetMainProject());
            this.project = project;
            this.isClose = isClose;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (isClose) {
                OpenProjects.getDefault().close(new Project[]{project});
            } else {
                OpenProjects.getDefault().setMainProject(project);
            }
        }
    }

    /**
     * Allows the user to use a keystroke to select what they are editing in the
     * project's tab. (Leave unimplemented for now)
     *
     * @param node
     * @param o
     * @return
     */
    @Override
    public Node findPath(Node node, Object o) {
        //leave unimplemented for now
        return null;
    }
}
