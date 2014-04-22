/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;

public class HiddenAttributeMismatchEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmpt = newProductCmpt(productCmptType, "ProductA");
    }

    @Test
    public void testDeltaTypeFix() {
        IAttribute newAttribute = productCmptType.newAttribute();
        newAttribute.setDefaultValue("defaultval");
        newAttribute.setName("attribute");
        IAttributeValue attrValue = new AttributeValue(productCmpt.getProductCmpt(), "Produkt", "attribute");
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "someValue"));
        HiddenAttributeMismatchEntry deltaEntry = new HiddenAttributeMismatchEntry(attrValue);

        deltaEntry.fix();

        assertEquals("defaultval", attrValue.getValueHolder().getValue().toString());
    }
}
