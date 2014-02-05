/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.context.messages.HtmlExportMessages;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;

/**
 * Represents a table with the attributes of an {@link IType} as rows and the attributes of the
 * attribute as columns
 * 
 * @author dicker
 * 
 */
public abstract class AttributesTablePageElement extends AbstractIpsObjectPartsContainerTablePageElement<IAttribute> {

    private IType type;

    /**
     * Creates an {@link AttributesTablePageElement} for the specified {@link IType}
     * 
     */
    public AttributesTablePageElement(IType type, List<IAttribute> attributes, DocumentationContext context) {
        super(attributes, context);
        this.setType(type);
        setId(type.getName() + "_" + "attributes"); //$NON-NLS-1$//$NON-NLS-2$
    }

    public AttributesTablePageElement(IType type, DocumentationContext context) {
        this(type, type.getAttributes(), context);
    }

    @Override
    protected List<IPageElement> createRowWithIpsObjectPart(IAttribute attribute) {
        IPageElement[] textPageElements = new PageElementUtils().createTextPageElements(getAttributeData(attribute));
        textPageElements[0].setAnchor(new PageElementUtils().createAnchorId(attribute));
        return Arrays.asList(textPageElements);
    }

    /**
     * returns a list with the values of the attributes of the attribute
     * 
     */
    protected List<String> getAttributeData(IAttribute attribute) {
        List<String> attributeData = new ArrayList<String>();

        attributeData.add(attribute.getName());
        attributeData.add(getContext().getLabel(attribute));
        attributeData.add(attribute.getDatatype());
        attributeData.add(attribute.getModifier().toString());

        try {
            attributeData.add(getContext().getDatatypeFormatter().formatValue(
                    getContext().getIpsProject().findValueDatatype(attribute.getDatatype()),
                    attribute.getDefaultValue()));
        } catch (CoreException e) {
            getContext().addStatus(new IpsStatus(IStatus.WARNING, "Unable to find ValueDatatype for attribute " //$NON-NLS-1$
                    + attribute.getName()));
            attributeData.add(attribute.getDefaultValue());
        }

        attributeData.add(getContext().getDescription(attribute));

        return attributeData;
    }

    @Override
    protected List<String> getHeadlineWithIpsObjectPart() {
        List<String> headline = new ArrayList<String>();

        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineName));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineLabel));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineDatatype));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineModifier));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineDefaultValue));
        headline.add(getContext().getMessage(HtmlExportMessages.AttributesTablePageElement_headlineDescription));

        return headline;
    }

    protected void setType(IType type) {
        this.type = type;
    }

    protected IType getType() {
        return type;
    }

}
