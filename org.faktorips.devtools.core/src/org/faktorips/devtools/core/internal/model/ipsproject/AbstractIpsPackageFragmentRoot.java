/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsPackageFragmentRoot extends IpsElement implements IIpsPackageFragmentRoot {

    /**
     * @param parent
     * @param name
     */
    public AbstractIpsPackageFragmentRoot(IIpsProject parent, String name) {
        super(parent, name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBasedOnSourceFolder() {
        try {
            return getIpsObjectPathEntry().getType()==IIpsObjectPathEntry.TYPE_SRC_FOLDER;
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBasedOnIpsArchive() {
        try {
            return getIpsObjectPathEntry().getType()==IIpsObjectPathEntry.TYPE_ARCHIVE;
        }
        catch (CoreException e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return (IIpsProject)parent;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getDefaultIpsPackageFragment() {
        return getIpsPackageFragment(""); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPathEntry getIpsObjectPathEntry() throws CoreException {
        IIpsObjectPathEntry entry = ((IpsProject)getIpsProject()).getIpsObjectPathInternal().getEntry(getName());
        if (entry!=null) {
            return entry;
        }
        throw new CoreException(new IpsStatus("No IpsObjectPathEntry found for package fragment root " + this)); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getIpsPackageFragment(String name) {
        if (isValidIpsPackageFragmentName(name)) {
            return newIpsPackageFragment(name);
        }
        return null;
    }
    
    /**
     * A valid IPS package fragment name is either the empty String for the default package fragment or a valid
     * package package fragment name according to <code>JavaConventions.validatePackageName</code>.
     */
    protected boolean isValidIpsPackageFragmentName(String name){
        try {
            return !getIpsProject().getNamingConventions().validateIpsPackageName(name).containsErrorMsg();
        }
        catch (CoreException e) {
            // nothing to do, will return false
        }
        return false;
    }

    protected abstract IIpsPackageFragment newIpsPackageFragment(String name);
    
    /**
     * {@inheritDoc}
     */
    public IIpsObject findIpsObject(IpsObjectType type, String qualifiedName) throws CoreException {
        return findIpsObject(new QualifiedNameType(qualifiedName, type));
    }

    /**
     * {@inheritDoc}
     */
    public final IIpsObject findIpsObject(QualifiedNameType qnt) throws CoreException {
        IIpsSrcFile file = findIpsSrcFile(qnt);
        if (file == null) {
            return null;
        }
        return file.getIpsObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public final IIpsSrcFile findIpsSrcFile(QualifiedNameType qnt) throws CoreException {
        IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
        if (pack==null) {
            return null;
        }
        IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    /**
     * Searches all objects of the given type in the root folder and adds them to the result.
     */
    abstract void findIpsObjects(IpsObjectType type, List result) throws CoreException;

    
}
