/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class DecimalToIntegerCgTest extends AbstractSingleConversionCgTest {

    private DecimalToIntegerCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new DecimalToIntegerCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("Integer.valueOf(decimal.intValue())", getConversionCode(converter, "decimal"));
    }
}