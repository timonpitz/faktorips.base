/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.refactor;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.refactor.RenameRefactoringProcessor;

/**
 * This is the "Rename Product Component Type Attribute" - refactoring.
 * 
 * @author Alexander Weickmann
 */
public final class RenameProductCmptTypeAttributeProcessor extends RenameRefactoringProcessor {

    /**
     * Creates a <tt>RenameProductCmptTypeAttributeProcessor</tt>.
     * 
     * @param productCmptTypeAttribute The <tt>IProductCmptTypeAttribute</tt> to be refactored.
     */
    protected RenameProductCmptTypeAttributeProcessor(IProductCmptTypeAttribute productCmptTypeAttribute) {
        super(productCmptTypeAttribute);
    }

    @Override
    protected void refactorModel(IProgressMonitor pm) throws CoreException {
        updateProductCmptReferences();
        changeAttributeName();
    }

    /**
     * Updates all references to the <tt>IProductCmptTypeAttribute</tt> in referencing
     * <tt>IProductCmpt</tt>s.
     */
    private void updateProductCmptReferences() throws CoreException {
        Set<IIpsSrcFile> productCmptSrcFiles = findReferencingSourceFiles(IpsObjectType.PRODUCT_CMPT);
        for (IIpsSrcFile ipsSrcFile : productCmptSrcFiles) {
            IProductCmpt productCmpt = (IProductCmpt)ipsSrcFile.getIpsObject();
            for (int i = 0; i < productCmpt.getNumOfGenerations(); i++) {
                IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(i);
                IAttributeValue attributeValue = generation.getAttributeValue(getOriginalElementName());
                if (attributeValue != null) {
                    attributeValue.setAttribute(getNewElementName());
                    addModifiedSrcFile(productCmpt.getIpsSrcFile());
                }
            }
        }
    }

    /**
     * Changes the name of the <tt>IProductCmptTypeAttribute</tt> to be refactored to the new name
     * provided by the user.
     */
    private void changeAttributeName() {
        getProductCmptTypeAttribute().setName(getNewElementName());
        addModifiedSrcFile(getProductCmptTypeAttribute().getIpsSrcFile());
    }

    /** Returns the <tt>IProductCmptTypeAttribute</tt> to be refactored. */
    private IProductCmptTypeAttribute getProductCmptTypeAttribute() {
        return (IProductCmptTypeAttribute)getIpsElement();
    }

    @Override
    public String getIdentifier() {
        return "RenameProductCmptTypeAttributeProcessor";
    }

    @Override
    public String getProcessorName() {
        return "Rename Product Component Type Attribute Refactoring Processor";
    }

    @Override
    public boolean isApplicable() throws CoreException {
        for (Object element : getElements()) {
            if (!(element instanceof IProductCmptTypeAttribute)) {
                return false;
            }
        }
        return true;
    }

}
