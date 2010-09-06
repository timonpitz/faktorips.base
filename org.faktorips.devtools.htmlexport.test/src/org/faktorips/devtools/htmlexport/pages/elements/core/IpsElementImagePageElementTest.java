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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAssociation;
import org.faktorips.devtools.core.internal.model.testcase.TestCase;
import org.faktorips.devtools.core.internal.model.testcasetype.TestCaseType;
import org.faktorips.devtools.core.internal.model.testcasetype.TestRuleParameter;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestValue;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestValueParameter;
import org.faktorips.devtools.htmlexport.pages.elements.types.IpsElementImagePageElement;
import org.faktorips.devtools.htmlexport.test.documentor.AbstractHtmlExportTest;

public class IpsElementImagePageElementTest extends AbstractHtmlExportTest {

    public void testPathPolicyCmptType() throws CoreException {
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(policyCmptType);
    }

    public void testPathProductCmptType() throws CoreException {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmptType);
    }

    public void testPathProductCmptTypeAssociation() throws CoreException {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        ProductCmptTypeAssociation association = new ProductCmptTypeAssociation(productCmptType, "xxx.BVBAsso");

        assertEquals(IpsObjectType.PRODUCT_CMPT.getFileExtension() + "assoc", new IpsElementImagePageElement(
                association).getFileName());
    }

    public void testPathPolicyCmpt() throws CoreException {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "xxx.BVB"); //$NON-NLS-1$
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "yyy.BVB"); //$NON-NLS-1$
        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmpt);
    }

    public void testPathPolicyCmptMitEigenemBild() throws CoreException {
        String productCmptTypeName = "xxx.BVB";
        ProductCmptType productCmptType = newProductCmptType(ipsProject, productCmptTypeName);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "yyy.BVB"); //$NON-NLS-1$

        productCmptType.setInstancesIcon("instanceicon");

        assertImagePathWorksWithIpsObjectAndIpsSrcFile(productCmptType);

        assertEquals(productCmptTypeName, new IpsElementImagePageElement(productCmpt).getFileName());
        assertEquals(productCmptTypeName, new IpsElementImagePageElement(productCmpt.getIpsSrcFile()).getFileName());
    }

    public void testPathTestObject() throws CoreException {
        TestCaseType testCaseType = newTestCaseType(ipsProject, "xxx.TestCaseType");
        TestCase testCase = newTestCase(testCaseType, "xxxTest"); //$NON-NLS-1$

        assertEquals("testrule", new IpsElementImagePageElement(testCase.newTestRule()).getFileName());
        assertEquals("testpolicycmpt", new IpsElementImagePageElement(testCase.newTestPolicyCmpt()).getFileName());
        ITestValue newTestValue = testCase.newTestValue();

        String testParameterName = "xyz.bbvbv";
        String testDatatype = "xyzDatatype";
        ITestValueParameter parameter = testCaseType.newInputTestValueParameter();

        parameter.setName(testParameterName);
        parameter.setDatatype(testDatatype);

        newTestValue.setTestValueParameter(testParameterName);

        assertEquals(testDatatype, new IpsElementImagePageElement(newTestValue).getFileName());
    }

    public void testPathTestParameter() throws CoreException {
        TestCaseType testCaseType = newTestCaseType(ipsProject, "xxx.TestCaseType");

        String testParameterName = "xyz.bbvbv";
        String testDatatype = "xyzDatatype";
        ITestValueParameter valueParameter = testCaseType.newInputTestValueParameter();

        valueParameter.setName(testParameterName);
        valueParameter.setDatatype(testDatatype);

        assertEquals(testDatatype, new IpsElementImagePageElement(valueParameter).getFileName());

        ITestPolicyCmptTypeParameter policyCmptTypeParameter = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        policyCmptTypeParameter.setDatatype(testDatatype);
        assertEquals(testDatatype, new IpsElementImagePageElement(policyCmptTypeParameter).getFileName());

        TestRuleParameter ruleParameter = testCaseType.newExpectedResultRuleParameter();
        assertEquals("testruleparameter", new IpsElementImagePageElement(ruleParameter).getFileName());
    }

    private void assertImagePathWorksWithIpsObjectAndIpsSrcFile(IpsObject ipsObject) {
        IpsElementImagePageElement elementIpsObject = new IpsElementImagePageElement(ipsObject);
        assertEquals(ipsObject.getIpsObjectType().getFileExtension(), elementIpsObject.getFileName());

        IpsElementImagePageElement elementIpsSrcFile = new IpsElementImagePageElement(ipsObject.getIpsSrcFile());
        assertEquals(ipsObject.getIpsObjectType().getFileExtension(), elementIpsSrcFile.getFileName());
    }

}