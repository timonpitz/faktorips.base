/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to save invalid extension properties and makes sure they could be stored to
 * XML the same way as they were initialized.
 * <p>
 * The different extensions of this class could handle different kinds of input data depending on
 * the kind of XML initialization.
 */
public abstract class ExtensionPropertyValue {

    private final String propertyId;

    private final IpsObjectPartContainer part;

    private boolean valueInitialized = false;

    private Object value;

    private ExtensionPropertyValue(String propertyId, IpsObjectPartContainer part) {
        this.propertyId = propertyId;
        this.part = part;
    }

    public static ExtensionPropertyValue createExtensionPropertyValue(String propertyId,
            Element valueElement,
            IpsObjectPartContainer part) {
        return new ExtensionPropertyXmlValue(propertyId, valueElement, part);
    }

    public static ExtensionPropertyValue createExtensionPropertyValue(String propertyId,
            String xmlStringValue,
            IpsObjectPartContainer part) {
        return new ExtensionPropertyStringValue(propertyId, xmlStringValue, part);
    }

    public String getPropertyId() {
        return propertyId;
    }

    public IpsObjectPartContainer getPart() {
        return part;
    }

    public void setValue(Object object) {
        this.value = object;
        valueInitialized = true;
    }

    public Object getValue() {
        if (!valueInitialized) {
            loadValue();
        }
        return value;
    }

    public void appendToXml(Element extPropertiesEl) {
        IExtensionPropertyDefinition extensionPropertyDefinition = getExtensionPropertyDefinition();
        Document ownerDocument = extPropertiesEl.getOwnerDocument();
        Element valueElement;
        if (extensionPropertyDefinition == null) {
            valueElement = getPreviouslyStoredXml(ownerDocument);
        } else {
            valueElement = createValueElement(getPropertyId(), extensionPropertyDefinition, getValue(), ownerDocument);
        }
        if (valueElement != null) {
            extPropertiesEl.appendChild(valueElement);
        }
    }

    protected IExtensionPropertyDefinition getExtensionPropertyDefinition() {
        IExtensionPropertyDefinition extensionPropertyDefinition = part.getExtensionPropertyDefinition(getPropertyId());
        return extensionPropertyDefinition;
    }

    protected Element createValueElement(String propertyId,
            IExtensionPropertyDefinition propertyDefinition,
            Object value,
            Document ownerDocument) {
        Element valueEl = ownerDocument.createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        if (value != null) {
            propertyDefinition.valueToXml(valueEl, value);
        }
        return valueEl;
    }

    protected abstract void loadValue();

    protected abstract Element getPreviouslyStoredXml(Document ownerDocument);

    protected void logMissingPropertyDefinition() {
        IpsPlugin.log(new IpsStatus(IStatus.WARNING, "Extension property " + getPropertyId() + " for " + getPart() //$NON-NLS-1$ //$NON-NLS-2$
                + " is unknown")); //$NON-NLS-1$
    }

    private static class ExtensionPropertyXmlValue extends ExtensionPropertyValue {

        private final Element valueElement;

        public ExtensionPropertyXmlValue(String propertyId, Element valueElement, IpsObjectPartContainer part) {
            super(propertyId, part);
            this.valueElement = valueElement;
        }

        @Override
        protected Element getPreviouslyStoredXml(Document ownerDocument) {
            if (valueElement == null) {
                return null;
            } else {
                Node importedNode = ownerDocument.importNode(valueElement, true);
                return (Element)importedNode;
            }
        }

        @Override
        protected void loadValue() {
            IExtensionPropertyDefinition propertyDefinition = getExtensionPropertyDefinition();
            String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
            if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
                if (propertyDefinition == null) {
                    logMissingPropertyDefinition();
                } else {
                    Object value = propertyDefinition.getValueFromXml(valueElement);
                    setValue(value);
                }
            }
        }

    }

    /**
     * This implementation of {@link ExtensionPropertyValue} takes a String from the XML
     * initialization and try to store it the same way to XML. To do so it uses a
     * {@link StringExtensionPropertyDefinition}.
     */
    private static class ExtensionPropertyStringValue extends ExtensionPropertyValue {

        private final String xmlStringValue;

        public ExtensionPropertyStringValue(String propertyId, String xmlStringValue, IpsObjectPartContainer part) {
            super(propertyId, part);
            this.xmlStringValue = xmlStringValue;
        }

        @Override
        protected Element getPreviouslyStoredXml(Document ownerDocument) {
            StringExtensionPropertyDefinition stringExtensionPropertyDefinition = new StringExtensionPropertyDefinition();
            return createValueElement(getPropertyId(), stringExtensionPropertyDefinition, xmlStringValue, ownerDocument);
        }

        @Override
        protected void loadValue() {
            IExtensionPropertyDefinition extensionPropertyDefinition = getExtensionPropertyDefinition();
            if (extensionPropertyDefinition != null) {
                Object valueFromString = extensionPropertyDefinition.getValueFromString(xmlStringValue);
                setValue(valueFromString);
            } else {
                logMissingPropertyDefinition();
            }
        }
    }

}
