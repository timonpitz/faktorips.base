package org.faktorips.devtools.htmlexport.actions;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.Documentor;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;

public class CreateHtmlExportAction extends ActionDelegate {

    private IStructuredSelection selection = StructuredSelection.EMPTY;

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectionChanged(IAction action, ISelection newSelection) {
        if (newSelection instanceof IStructuredSelection) {
            selection = (IStructuredSelection)newSelection;
        } else {
            selection = StructuredSelection.EMPTY;
        }
    }

    private IIpsProject getIpsProject() {
        if (selection.size() != 1) {
            return null;
        }
        if (selection.getFirstElement() instanceof PlatformObject) {
            IProject project = (IProject)((PlatformObject)selection.getFirstElement()).getAdapter(IProject.class);
            if (project == null) {
                return null;
            }
            IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
            return ipsModel.getIpsProject(project.getProject());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IAction action) {
        if (selection.size() > 1) {
            MessageDialog.openInformation(getShell(), Messages.CreateHtmlExportAction_HtmlExport,
                    Messages.CreateHtmlExportAction_SelectJustOneProject);
            return;
        }
        IIpsProject ipsProject = getIpsProject();
        if (ipsProject == null) {
            MessageDialog.openInformation(getShell(), Messages.CreateHtmlExportAction_HtmlExport,
                    Messages.CreateHtmlExportAction_SelectOneProject);
            return;
        }

        String selected = getDestinationFolder();

        if (new File(selected).isDirectory()) {
            exportHtml(selected);
        }

    }

    private void exportHtml(String selected) {

        DocumentorConfiguration documentorConfig = new DocumentorConfiguration();

        documentorConfig.setPath(selected);
        documentorConfig.setIpsProject(getIpsProject());
        documentorConfig.setLayouter(new HtmlLayouter(".resource"));

        documentorConfig.addDocumentorScript(new StandardDocumentorScript());
        documentorConfig.setLinkedIpsObjectClasses(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());

        new Documentor(documentorConfig).execute();
    }

    private String getDestinationFolder() {
        DirectoryDialog fd = createFileSaveDialog();
        return fd.open();
    }

    private DirectoryDialog createFileSaveDialog() {
        DirectoryDialog fd = new DirectoryDialog(getShell());
        fd.setText(Messages.CreateHtmlExportAction_Export);
        fd.setFilterPath(getIpsProject().getProject().getLocation() + File.separator + "html");
        return fd;
    }

    /**
     * Returns the active shell.
     */
    protected Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

}
