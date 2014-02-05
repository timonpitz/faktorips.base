/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.DecimalHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.junit.Before;
import org.junit.Test;

public class IpsModelImplTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IpsModel model;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        model = (IpsModel)ipsProject.getIpsModel();
    }

    @Test
    public void testGetIpsObjectPath() throws CoreException {
        IIpsObjectPath path = ipsProject.getIpsObjectPath();
        path.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("newpackage");
        ipsProject.setIpsObjectPath(path);

        // path is created in the first call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());

        // path is read from the cache in the second call
        path = ipsProject.getIpsObjectPath();
        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());

        IIpsProject secondProject = newIpsProject("TestProject2");
        IIpsObjectPath secondPath = secondProject.getIpsObjectPath();
        secondPath.getSourceFolderEntries()[0].setSpecificBasePackageNameForMergableJavaClasses("secondpackage");
        secondProject.setIpsObjectPath(secondPath);

        assertEquals("newpackage", path.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());
        assertEquals("secondpackage",
                secondPath.getSourceFolderEntries()[0].getSpecificBasePackageNameForMergableJavaClasses());
    }

    @Test
    public void testGetDatatypeHelpers() throws CoreException {
        IIpsProjectProperties props = ipsProject.getProperties();
        props.setPredefinedDatatypesUsed(new String[] { Datatype.DECIMAL.getQualifiedName() });
        ipsProject.setProperties(props);
        DatatypeHelper helper = model.getDatatypeHelper(ipsProject, Datatype.DECIMAL);
        assertEquals(DecimalHelper.class, helper.getClass());
        assertNull(model.getDatatypeHelper(ipsProject, Datatype.MONEY));
    }

}
