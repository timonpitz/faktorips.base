/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.context;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;

/**
 * Encapsulates accesses to the IpsPlugin
 * 
 * @author dicker
 */
public interface IPluginResourceFacade {

    public IpsObjectType[] getDefaultIpsObjectTypes();

    public void log(IStatus status);

    public String getIpsPluginPluginId();

    public UIDatatypeFormatter getDatatypeFormatter();

    public Properties getMessageProperties(String resourceName) throws CoreException;

}