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

import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * An implementation of {@link AbstractSearch} in order to process {@link IpsProjectRefEntry}
 */
public class ProjectSearch extends AbstractSearch {

    private List<IIpsProject> projects = new ArrayList<IIpsProject>();

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        if (isProjectRefEntry(entry) && getReferencedIpsProject(entry) != null) {
            projects.add(getReferencedIpsProject(entry));
        }
    }

    public List<IIpsProject> getProjects() {
        return projects;
    }

}
