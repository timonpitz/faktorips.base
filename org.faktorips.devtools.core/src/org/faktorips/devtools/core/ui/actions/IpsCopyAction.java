package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyResourceAction;
import org.eclipse.ui.part.ResourceTransfer;
import org.faktorips.devtools.core.internal.model.IpsObjectPartState;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 * Copy of objects controlled by FaktorIps. 
 * 
 * @author Thorsten Guenther
 */
public class IpsCopyAction extends IpsAction {

    private Clipboard clipboard;
    private Shell shell;
    
    public IpsCopyAction(ISelectionProvider selectionProvider, Shell shell) {
        super(selectionProvider);
        clipboard = new Clipboard(shell.getDisplay());
        this.shell = shell;
    }

    public void run(IStructuredSelection selection) {
        List selectedObjects = selection.toList();

        List copiedObjects = new ArrayList();
        List copiedResources = new ArrayList();
        IIpsObjectPart part;
        for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
            Object selected = iter.next();

            if (selected instanceof IIpsObjectPart) {
                part = (IIpsObjectPart)selected;
                copiedObjects.add(new IpsObjectPartState(part).toString());
            }
            else if (selected instanceof IIpsElement) {
            	
            	IResource resource = ((IIpsElement)selected).getCorrespondingResource();
            	if (resource != null) {
            		System.out.println("about to copy " + resource);
            		copiedResources.add(resource);
            	}
            }
        }

        if (copiedObjects.size() > 0 || copiedResources.size() > 0) {
            //clipboard.setContents(getDataArray(copiedObjects, copiedResources), getTypeArray(copiedObjects, copiedResources));

            clipboard.setContents(copiedResources.toArray(), new Transfer[] {ResourceTransfer.getInstance()});
     
    		final Object[] result= new Object[1];
    		shell.getDisplay().syncExec(new Runnable() {
    			public void run() {
    				result[0]= clipboard.getContents(ResourceTransfer.getInstance());
    			}
    		});

            Object o = clipboard.getContents(ResourceTransfer.getInstance());
            System.out.println("found in clipboard: " + o + " / " + result[0]);
        }
    }
}
