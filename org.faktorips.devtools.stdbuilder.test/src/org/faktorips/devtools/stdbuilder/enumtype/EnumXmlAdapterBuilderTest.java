/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class EnumXmlAdapterBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumXmlAdapterBuilder builder;

    private IEnumType enumType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        builder = new EnumXmlAdapterBuilder(builderSet);
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
        enumType.setExtensible(true);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertFalse(generatedJavaElements.contains(getGeneratedJavaXmlAdapter()));
    }

    private IType getGeneratedJavaXmlAdapter() {
        return getGeneratedJavaClass(enumType, true, ENUM_TYPE_NAME + "XmlAdapter");
    }

    @Test
    public void testIsBuilderFor() throws CoreException {
        assertThat(builder.isBuilderFor(enumType.getIpsSrcFile()), is(true));

        enumType.setExtensible(false);
        assertThat(builder.isBuilderFor(enumType.getIpsSrcFile()), is(false));

        enumType.setAbstract(true);
        enumType.setExtensible(true);
        assertThat(builder.isBuilderFor(enumType.getIpsSrcFile()), is(false));
    }
}
