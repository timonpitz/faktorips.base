/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition2;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for all purposes of an {@link IIpsObjectPartContainer} that relates to
 * extension properties.
 * <p>
 * Extension properties are additional properties that could be registered via extension point in
 * other plugIns. They are used to store additional information at specific sections of the model or
 * of the product component.
 * <p>
 * Every {@link IpsObjectPartContainer} has to instantiate its own {@link ExtensionPropertyHandler}.
 * 
 */
public class ExtensionPropertyHandler {

    private final IpsObjectPartContainer ipsObjectPartContainer;

    /**
     * Map containing invalid extension property IDs as keys and their values. Only used to not
     * loose these information. They are initialized while reading the xml and stored when writing -
     * nothing else.
     */
    private final Map<String, ExtensionPropertyValue> extPropertiyValuesMap = new ConcurrentHashMap<String, ExtensionPropertyValue>();

    /**
     * Create a new {@link ExtensionPropertyHandler} for the given {@link IIpsObjectPartContainer}
     * 
     */
    public ExtensionPropertyHandler(IpsObjectPartContainer ipsObjectPartContainer) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
    }

    /**
     * Get the value for the extension property specified by the ID. If no value is set so far, the
     * default value is returned.
     * 
     * @param propertyId The id of the extension property for which you want to get the value
     * @return The value object depending on the extension property definition
     * @throws IllegalArgumentException if the given property id specifies no valid extension
     *             property definition
     */
    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        initMissingExtProperties();
        ExtensionPropertyValue extensionPropertyValue = extPropertiyValuesMap.get(propertyId);
        if (extensionPropertyValue == null) {
            throw new IllegalArgumentException("There is no extension property definition for id " + propertyId); //$NON-NLS-1$
        }
        return extensionPropertyValue.getValue();
    }

    /**
     * Checks whether the extension property with the given ID is defined.
     */
    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        return ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId) != null;
    }

    /**
     * Set a new value for the extension property specified by the propertyId. The kind of object
     * depends on the extension property definition.
     * <p>
     * Before setting the value
     * {@link IExtensionPropertyDefinition#beforeSetValue(IIpsObjectPartContainer, Object)} is
     * called. If this method returns false the method does nothing more and returns.
     * <p>
     * The value is only set if it is not already stored, using the objects equal method to compare
     * both objects. In case of a new value a change event is triggered on the
     * {@link IpsObjectPartContainer}.
     * <p>
     * After setting the new value, even if it was not a new one, the method
     * {@link IExtensionPropertyDefinition#afterSetValue(IIpsObjectPartContainer, Object)} is
     * called.
     * 
     * @param propertyId The id of the extension property for which the value should be set
     * @param value The value that should be set for the extension property
     * @throws IllegalArgumentException if the given property id specifies no valid extension
     *             property definition
     */
    public void setExtPropertyValue(String propertyId, Object value) {
        checkExtProperty(propertyId);
        IExtensionPropertyDefinition property = ipsObjectPartContainer.getExtensionPropertyDefinition(propertyId);
        if (!property.beforeSetValue(ipsObjectPartContainer, value)) {
            // veto to set the new value by the property definition
            return;
        }
        initMissingExtProperties();
        if (!ObjectUtils.equals(value, getExtPropertyValue(propertyId))) {
            ExtensionPropertyValue propertyValue = extPropertiyValuesMap.get(propertyId);
            propertyValue.setValue(value);
            ipsObjectPartContainer.objectHasChanged();
        }
        property.afterSetValue(ipsObjectPartContainer, value);
    }

    /* private */void checkExtProperty(String propertyId) {
        if (!isExtPropertyDefinitionAvailable(propertyId)) {
            throw new IllegalArgumentException("Extension property " + propertyId + " is not defined for type " //$NON-NLS-1$ //$NON-NLS-2$
                    + getClass().getName());
        }
    }

    /* private */void initMissingExtProperties() {
        Collection<IExtensionPropertyDefinition> properties = ipsObjectPartContainer.getExtensionPropertyDefinitions();
        for (IExtensionPropertyDefinition propertyDefinition : properties) {
            if (extPropertiyValuesMap.get(propertyDefinition.getPropertyId()) == null) {
                Object defaultValue = getExtensionPropertyDefaultValue(propertyDefinition);
                ExtensionPropertyValue extensionPropValue = ExtensionPropertyValue.createExtensionPropertyValue(
                        propertyDefinition.getPropertyId(), (Element)null, ipsObjectPartContainer);
                extensionPropValue.setValue(defaultValue);
                extPropertiyValuesMap.put(propertyDefinition.getPropertyId(), extensionPropValue);
            }
        }
    }

    private Object getExtensionPropertyDefaultValue(IExtensionPropertyDefinition property) {
        if (property instanceof IExtensionPropertyDefinition2) {
            return ((IExtensionPropertyDefinition2)property).getDefaultValue(ipsObjectPartContainer);
        } else {
            return property.getDefaultValue();
        }
    }

    /**
     * Stores all the extension property values to the given {@link Element}. Extension property
     * values without a valid {@link IExtensionPropertyDefinition} are stored as they are read.
     * 
     */
    public void toXml(Element element) {
        Collection<IExtensionPropertyDefinition> propertyDefinitions = ipsObjectPartContainer
                .getExtensionPropertyDefinitions();
        if (propertyDefinitions.isEmpty() && extPropertiyValuesMap.isEmpty()) {
            return;
        }
        initMissingExtProperties();
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        for (ExtensionPropertyValue propertyValue : extPropertiyValuesMap.values()) {
            propertyValue.appendToXml(extPropertiesEl);
        }
        if (extPropertiesEl.hasChildNodes()) {
            element.appendChild(extPropertiesEl);
        }
    }

    public boolean isEqualDefaultValue(Entry<String, Object> entry,
            Collection<IExtensionPropertyDefinition> propertyDefinitions) {
        for (IExtensionPropertyDefinition property : propertyDefinitions) {
            if (entry.getKey().equals(property.getPropertyId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property doesn't exists as definitions then the property will be ignored.
     * <p>
     * Note: Better do not use this method. The extension property should be initialized by
     * {@link #initPropertyFromXml(Element)}.
     * 
     * @param propertyId id of the extension property
     * @param extPropertyValue extension property value
     */
    protected void addExtensionPropertyValue(String propertyId, String extPropertyValue) {
        initMissingExtProperties();
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(propertyId,
                extPropertyValue, ipsObjectPartContainer);
        extPropertiyValuesMap.put(propertyId, extensionPropertyValue);
        extensionPropertyValue.loadValue();
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     * 
     * @param containerEl The &lt;ExtensionProperties&gt; element.
     */
    public void initFromXml(Element containerEl) {
        extPropertiyValuesMap.clear();
        initMissingExtProperties();
        Element extPropertiesEl = XmlUtil.getFirstElement(containerEl,
                IpsObjectPartContainer.XML_EXT_PROPERTIES_ELEMENT);
        if (extPropertiesEl == null) {
            return;
        }
        NodeList nl = extPropertiesEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getNodeName().equals(IpsObjectPartContainer.XML_VALUE_ELEMENT)) {
                initPropertyFromXml((Element)node);
            }
        }
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     */
    protected void initPropertyFromXml(Element valueElement) {
        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        ExtensionPropertyValue extensionPropertyValue = ExtensionPropertyValue.createExtensionPropertyValue(propertyId,
                valueElement, ipsObjectPartContainer);
        extensionPropertyValue.loadValue();
        extPropertiyValuesMap.put(propertyId, extensionPropertyValue);
    }

    /**
     * Validates the extension property values.
     * 
     * @throws CoreException if an error occurs while validating the extension properties.
     */
    public MessageList validate() throws CoreException {
        MessageList ml = new MessageList();
        Collection<IExtensionPropertyDefinition> properties = ipsObjectPartContainer.getExtensionPropertyDefinitions();
        for (IExtensionPropertyDefinition property : properties) {
            Object value = getExtPropertyValue(property.getPropertyId());
            MessageList newList = property.validate(ipsObjectPartContainer, value);
            if (newList != null) {
                ml.add(newList);
            }
        }
        return ml;
    }

    protected Map<String, ExtensionPropertyValue> getExtPropertyValuesMap() {
        return extPropertiyValuesMap;
    }

    public void clear() {
        extPropertiyValuesMap.clear();
    }
}