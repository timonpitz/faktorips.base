/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.formulatest;

import java.util.GregorianCalendar;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

public class FormulaTestBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmpt productCmpt;
    private IConfigElement configElement;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "policyCmpt");
        productCmpt = newProductCmpt(ipsProject, "productCmpt");
        productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(new GregorianCalendar());
        configElement = generation.newConfigElement();
        configElement.setType(ConfigElementType.FORMULA);
        configElement.newFormulaTestCase();
    }

    public void testDelete() throws CoreException{
        productCmpt.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        
        assertTrue(productCmpt.getIpsSrcFile().exists());

        productCmpt.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertFalse(productCmpt.getIpsSrcFile().exists());
        
        IProductCmpt productCmpt2 = newProductCmpt(ipsProject, "productCmptWithoutFormula");
        productCmpt2.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        productCmpt2.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }
}
