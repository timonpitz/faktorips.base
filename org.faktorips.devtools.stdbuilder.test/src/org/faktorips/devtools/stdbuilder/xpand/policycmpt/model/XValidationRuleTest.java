/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XValidationRuleTest {

    @Mock
    private GeneratorModelContext context;

    @Mock
    private ModelService modelService;

    @Mock
    private IValidationRule validationRule;

    private XValidationRule xValidationRule;

    @Before
    public void createXValidationRule() throws Exception {
        xValidationRule = new XValidationRule(validationRule, context, modelService);
    }

    @Test
    public void testConvertToJavaParameters() throws Exception {
        LinkedHashSet<String> parameters = new LinkedHashSet<String>();
        LinkedHashSet<String> javaParameters = xValidationRule.convertToJavaParameters(parameters);
        assertTrue(javaParameters.isEmpty());

        parameters.add("asd");
        javaParameters = xValidationRule.convertToJavaParameters(parameters);
        Iterator<String> iterator = javaParameters.iterator();
        assertTrue(javaParameters.size() == 1);
        assertEquals("asd", iterator.next());

        parameters.add("0");
        javaParameters = xValidationRule.convertToJavaParameters(parameters);
        iterator = javaParameters.iterator();
        assertTrue(javaParameters.size() == 2);
        assertEquals("asd", iterator.next());
        assertEquals("p0", iterator.next());

        parameters.add("p0");
        javaParameters = xValidationRule.convertToJavaParameters(parameters);
        iterator = javaParameters.iterator();
        assertTrue(javaParameters.size() == 3);
        assertEquals("asd", iterator.next());
        assertEquals("pp0", iterator.next());
        assertEquals("p0", iterator.next());
    }
}
