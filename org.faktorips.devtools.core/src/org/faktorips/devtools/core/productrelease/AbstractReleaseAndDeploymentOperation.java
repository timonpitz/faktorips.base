/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.productrelease;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;

public abstract class AbstractReleaseAndDeploymentOperation implements IReleaseAndDeploymentOperation {

    @Override
    public List<ITargetSystem> getAvailableTargetSystems() {
        return new ArrayList<ITargetSystem>();
    }

    @Override
    public List<IFile> additionalResourcesToCommit() {
        return new ArrayList<IFile>();
    }

    @Override
    public boolean customReleaseSettings(IIpsProject ipsProject,
            IProgressMonitor progressMonitor,
            MessageList messageList) {
        return true;
    }

}