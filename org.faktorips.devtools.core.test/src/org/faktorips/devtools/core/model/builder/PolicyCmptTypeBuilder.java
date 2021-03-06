/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.StringUtil;

public class PolicyCmptTypeBuilder {

    private final IIpsProject project;
    private final String name;

    private IPolicyCmptType supertype;

    public PolicyCmptTypeBuilder(IIpsProject project, String name) {
        this.project = project;
        this.name = name;
    }

    public PolicyCmptTypeBuilder withSupertype(IPolicyCmptType supertype) {
        this.supertype = requireNonNull(supertype);
        return this;
    }

    public IPolicyCmptType build() {
        try {
            PolicyCmptType result = (PolicyCmptType)newIpsObject(project.getIpsPackageFragmentRoots()[0],
                    IpsObjectType.POLICY_CMPT_TYPE,
                    name, false);

            if (supertype != null) {
                result.setSupertype(supertype.getQualifiedName());
            }

            return result;
        } catch (CoreException e) {
            throw new RuntimeException("Failed to create PolicyCmptType", e);
        }
    }

    private IIpsObject newIpsObject(final IIpsPackageFragmentRoot root,
            final IpsObjectType type,
            final String qualifiedName,
            final boolean createAutoProductCmptType) throws CoreException {

        final String packName = StringUtil.getPackageName(qualifiedName);
        final String unqualifiedName = StringUtil.unqualifiedName(qualifiedName);
        IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
                if (!pack.exists()) {
                    pack = root.createPackageFragment(packName, true, null);
                }
                IIpsSrcFile file = pack.createIpsFile(type, unqualifiedName, true, null);
                IIpsObject ipsObject = file.getIpsObject();

                if (!createAutoProductCmptType && ipsObject instanceof IPolicyCmptType) {
                    ((IPolicyCmptType)ipsObject).setConfigurableByProductCmptType(false);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
        IIpsPackageFragment pack = root.getIpsPackageFragment(packName);
        return pack.getIpsSrcFile(type.getFileName(unqualifiedName)).getIpsObject();
    }

}
