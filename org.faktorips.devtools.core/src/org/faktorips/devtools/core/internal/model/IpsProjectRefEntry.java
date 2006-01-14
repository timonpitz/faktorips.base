package org.faktorips.devtools.core.internal.model;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IpsProjectRefEntry.
 *  
 * @author Jan Ortmann
 */
public class IpsProjectRefEntry extends IpsObjectPathEntry implements
        IIpsProjectRefEntry {
    
    // the ips project referenced by this entry
    private IIpsProject referencedIpsProject;
    
    IpsProjectRefEntry(IpsObjectPath path) {
        super(path);
    }
    
    IpsProjectRefEntry(IpsObjectPath path, IIpsProject referencedIpsProject) {
        super(path);
        ArgumentCheck.notNull(referencedIpsProject);
        this.referencedIpsProject = referencedIpsProject;
    }

    /**
     * Overridden.
     */
    public IIpsProject getReferencedIpsProject() {
        return referencedIpsProject;
    }

    /**
     * Overridden
     */
    public String getType() {
        return TYPE_PROJECT_REFERENCE;
    }

    /**
     * Overridden
     */
    public IIpsObject findIpsObject(IIpsProject project, IpsObjectType type, String qualifiedName)
            throws CoreException {
        return referencedIpsProject.findIpsObject(type, qualifiedName);
    }

    /**
     * Overridden.
     */
    public IIpsObject findIpsObject(IIpsProject project, QualifiedNameType nameType)
            throws CoreException {
        return referencedIpsProject.findIpsObject(nameType);
    }

    /**
     * Overridden.
     */
    public void findIpsObjects(IIpsProject project, IpsObjectType type, List result)
            throws CoreException {
        ((IpsProject)referencedIpsProject).findIpsObjects(type, result);
    }

    /**
     * Overridden.
     */
    public void findIpsObjectsStartingWith(IIpsProject project, IpsObjectType type, String prefix, boolean ignoreCase, List result)
            throws CoreException {
        ((IpsProject)referencedIpsProject).findIpsObjectsStartingWith(type, prefix, ignoreCase, result);
    }
    
    /**
     * Overridden.
     */
    public void initFromXml(Element element, IProject project) {
        String projectName = element.getAttribute("referencedIpsProject");
        referencedIpsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
    }

    /**
     * Overridden.
     */
    public Element toXml(Document doc) {
        Element element = doc.createElement(XML_ELEMENT);
        element.setAttribute("type", TYPE_PROJECT_REFERENCE);
        element.setAttribute("referencedIpsProject", referencedIpsProject.getName());
        return element;
    }

}
