/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.LibraryIpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsLibraryEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
import org.faktorips.values.ObjectUtil;

/**
 * {@link IIpsPackageFragmentRoot} for Libraries.
 * 
 * @author Jan Ortmann
 */
public class LibraryIpsPackageFragmentRoot extends AbstractIpsPackageFragmentRoot {

    private IIpsStorage storage;

    public LibraryIpsPackageFragmentRoot(IIpsProject ipsProject, IIpsStorage storage) {
        super(ipsProject, storage.getName());
        this.storage = storage;
    }

    @Override
    public IIpsStorage getIpsStorage() {
        return storage;
    }

    @Override
    public boolean exists() {
        if (getIpsStorage() == null) {
            return false;
        }
        return getIpsStorage().exists();
    }

    @Override
    public IPackageFragmentRoot getArtefactDestination(boolean derived) throws CoreException {
        IIpsLibraryEntry entry = (IIpsLibraryEntry)getIpsObjectPathEntry();
        String path;
        if (entry.getPath().isAbsolute()) {
            path = entry.getPath().toPortableString();
        } else {
            path = getIpsProject().getProject().getLocation().append(entry.getPath()).toPortableString();
        }
        return getIpsProject().getJavaProject().getPackageFragmentRoot(path);
    }

    @Override
    public IIpsPackageFragment[] getIpsPackageFragments() throws CoreException {
        List<IIpsPackageFragment> list = getIpsPackageFragmentsAsList();
        return list.toArray(new IIpsPackageFragment[list.size()]);
    }

    private List<IIpsPackageFragment> getIpsPackageFragmentsAsList() throws CoreException {
        if (getIpsStorage() == null) {
            return new ArrayList<IIpsPackageFragment>(0);
        }

        String[] packNames = storage.getNonEmptyPackages();
        List<IIpsPackageFragment> list = new ArrayList<IIpsPackageFragment>(packNames.length);
        for (String packName : packNames) {
            list.add(new LibraryIpsPackageFragment(this, packName));
        }

        return list;
    }

    @Override
    protected IIpsPackageFragment newIpsPackageFragment(String name) {
        return new LibraryIpsPackageFragment(this, name);
    }

    @Override
    public IResource[] getNonIpsResources() throws CoreException {
        return new IResource[0];
    }

    @Override
    public IIpsPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor)
            throws CoreException {

        throw newExceptionMethodNotAvailableForArchvies();
    }

    @Override
    public IResource getCorrespondingResource() {
        return storage.getCorrespondingResource();
    }

    private CoreException newExceptionMethodNotAvailableForArchvies() {
        return new CoreException(new IpsStatus("Not possible for archives because they are not modifiable.")); //$NON-NLS-1$
    }

    @Override
    void findIpsSourceFiles(IpsObjectType type, String packageFragment, List<IIpsSrcFile> result) throws CoreException {
        if (type == null) {
            return;
        }
        if (storage == null) {
            return;
        }
        Set<QualifiedNameType> qntSet = storage.getQNameTypes();
        for (QualifiedNameType qnt : qntSet) {
            if (!type.equals(qnt.getIpsObjectType())) {
                continue;
            }
            if (packageFragment != null && !qnt.getPackageName().equals(packageFragment)) {
                continue;
            }
            IIpsPackageFragment pack = getIpsPackageFragment(qnt.getPackageName());
            if (pack == null) {
                return;
            }
            IIpsSrcFile file = pack.getIpsSrcFile(qnt.getFileName());
            if (file.exists()) {
                result.add(file);
            }
        }
    }

    @Override
    public boolean isContainedInArchive() {
        if (getIpsStorage() == null) {
            return false;
        }
        return !getIpsStorage().isFolder();
    }

    @Override
    public void delete() throws CoreException {
        throw new UnsupportedOperationException("IPS Package Fragment Roots that are stored" + //$NON-NLS-1$
                " in an archive cannot be deleted."); //$NON-NLS-1$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if two objects are "equal" without considering the parent. If {@link LibraryIpsSrcFile
     * LibraryIpsSrcFiles} from different projects refer the same jar file and the
     * {@link LibraryIpsPackageFragmentRoot} is the same but the {@link IIpsProject} is different,
     * the default implementation in {@link IIpsElement} may yield misleadingly <code>false</code>.
     * Therefore we need to overwrite the default implementation in {@link IIpsElement}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LibraryIpsPackageFragmentRoot other = (LibraryIpsPackageFragmentRoot)obj;
        return ObjectUtil.equals(storage.getLocation(), other.storage.getLocation());
    }

}
