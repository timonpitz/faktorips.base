/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ValidationResultCache;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.memento.XmlMemento;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for easy implementation of {@link IIpsObjectPartContainer}.
 * <p>
 * <strong>Subclassing:</strong><br>
 * This base class provides the implementations required by the interfaces {@link IDescribedElement}
 * and {@link ILabeledElement}. Therefore, subclasses are able to receive multi-language support
 * just by implementing the corresponding interfaces.
 * 
 * @see IIpsObjectPartContainer
 * 
 * @author Jan Ortmann
 */
public abstract class IpsObjectPartContainer extends IpsElement implements IIpsObjectPartContainer {

    /** Name of the XML element the containing the elements for the extension property values. */
    protected static final String XML_EXT_PROPERTIES_ELEMENT = "ExtensionProperties"; //$NON-NLS-1$

    /** Name of the XML element containing a property value. */
    protected static final String XML_VALUE_ELEMENT = "Value"; //$NON-NLS-1$

    /** Name of the value element's attribute that stores the property id. */
    protected static final String XML_ATTRIBUTE_EXTPROPERTYID = "id"; //$NON-NLS-1$

    /**
     * Name of the value element's attribute that stores the information if the value is
     * <code>null</code> or not.
     */
    protected static final String XML_ATTRIBUTE_ISNULL = "isNull"; //$NON-NLS-1$

    /** List containing all labels attached to the object part container. */
    private final List<ILabel> labels = new ArrayList<ILabel>(2);

    /** List containing all descriptions attached to the object part container. */
    private final List<IDescription> descriptions = new ArrayList<IDescription>(2);

    /** Map containing extension property IDs as keys and their values. */
    private HashMap<String, Object> extPropertyValues = null;

    /**
     * Map containing invalid extension property IDs as keys and their values. Only used to not
     * loose these information. They are initialized while reading the xml and stored when writing -
     * nothing else.
     */
    private Map<String, Node> invalidExtPropertyValues = new LinkedHashMap<String, Node>();

    /** Validation start time used for tracing in debug mode */
    private long validationStartTime;

    public IpsObjectPartContainer(IIpsElement parent, String name) {
        super(parent, name);
        if (this instanceof ILabeledElement || this instanceof IDescribedElement) {
            initLabelsAndDescriptions();
        }
    }

    /**
     * Only for testing purposes.
     */
    public IpsObjectPartContainer() {
        this(null, null);
    }

    private void initLabelsAndDescriptions() {
        IIpsProject ipsProject = getIpsProject();
        if (ipsProject != null) {
            for (ISupportedLanguage language : ipsProject.getReadOnlyProperties().getSupportedLanguages()) {
                Locale locale = language.getLocale();
                if (this instanceof ILabeledElement) {
                    Label label = (Label)newLabel();
                    label.setLocaleWithoutChangeEvent(locale);
                }
                if (this instanceof IDescribedElement) {
                    Description description = (Description)newDescription();
                    description.setLocaleWithoutChangeEvent(locale);
                }
            }
        }
    }

    @Override
    public IIpsSrcFile getIpsSrcFile() {
        IIpsObject obj = getIpsObject();
        if (obj == null) {
            return null;
        }
        return obj.getIpsSrcFile();
    }

    /**
     * This implementation returns the labels and descriptions of the IPS object part container. It
     * might be extended by subclasses using the method {@link #getChildrenThis()}.
     */
    @Override
    public final IIpsElement[] getChildren() {
        List<IIpsElement> children = new ArrayList<IIpsElement>(labels.size() + descriptions.size());
        children.addAll(labels);
        children.addAll(descriptions);
        IIpsElement[] subclassChildren = getChildrenThis();
        children.addAll(Arrays.asList(subclassChildren));
        return children.toArray(new IIpsElement[children.size()]);
    }

    /**
     * Subclass implementation that can be used to extend the method {@link #getChildren()}. Must
     * not return <tt>null</tt>.
     */
    protected abstract IIpsElement[] getChildrenThis();

    /**
     * Returns the id that can be used for a new part, so that its id is unique.
     */
    protected String getNextPartId() {
        return getIpsModel().getNextPartId(this);
    }

    @Override
    public Object getExtPropertyValue(String propertyId) {
        checkExtProperty(propertyId);
        initExtPropertiesIfNotDoneSoFar();
        return extPropertyValues.get(propertyId);
    }

    @Override
    public boolean isExtPropertyDefinitionAvailable(String propertyId) {
        return getExtensionPropertyDefinition(propertyId) != null;
    }

    @Override
    public void setExtPropertyValue(String propertyId, Object value) {
        checkExtProperty(propertyId);
        IExtensionPropertyDefinition property = getExtensionPropertyDefinition(propertyId);
        if (!property.beforeSetValue(this, value)) {
            // veto to set the new value by the property definition
            return;
        }
        initExtPropertiesIfNotDoneSoFar();
        if (!ObjectUtils.equals(value, getExtPropertyValue(propertyId))) {
            extPropertyValues.put(propertyId, value);
            objectHasChanged();
        }
        property.afterSetValue(this, value);
    }

    /**
     * Informs the entire system that this model object has been changed. Subclasses should fire
     * this event if any property of the object changes.
     * 
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     * 
     * @return A flag indicating whether the valueChanged event has been fired successfully (it does
     *         not if the old value and new value are considered to be equal).
     */
    protected final boolean valueChanged(Object oldValue, Object newValue) {
        boolean changed = !ObjectUtils.equals(oldValue, newValue);
        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * Informs the entire system that this model object has been changed. Subclasses should fire
     * this event if any property of the object changes.
     * 
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     * 
     * @return A flag indicating whether the valueChanged event has been fired successfully (it does
     *         not if the old value and new value are considered to be equal).
     */
    protected final boolean valueChanged(Object oldValue, Object newValue, String propertyName) {
        boolean changed = !ObjectUtils.equals(oldValue, newValue);
        if (changed) {
            objectHasChanged(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
        }

        return changed;
    }

    /**
     * @see #valueChanged(Object, Object)
     */
    protected final boolean valueChanged(boolean oldValue, boolean newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * @see #valueChanged(Object, Object)
     */
    protected final boolean valueChanged(int oldValue, int newValue) {
        boolean changed = oldValue != newValue;
        if (changed) {
            objectHasChanged();
        }

        return changed;
    }

    /**
     * Called when the object's state has changed to inform about this.
     */
    protected abstract void objectHasChanged();

    /**
     * Called when the object's state has changed to inform about this.
     * 
     * @param propertyChangeEvent the propertyChangeEvent details the change.
     */
    protected abstract void objectHasChanged(PropertyChangeEvent propertyChangeEvent);

    /**
     * Has to be called when a part was added to the container to trigger event notification.
     */
    protected void partWasAdded(IIpsObjectPart part) {
        objectHasChanged(ContentChangeEvent.newPartAddedEvent(part));
    }

    protected void partsMoved(IIpsObjectPart[] parts) {
        ContentChangeEvent event = ContentChangeEvent.newPartsChangedPositionsChangedEvent(getIpsSrcFile(), parts);
        objectHasChanged(event);
    }

    protected void objectHasChanged(ContentChangeEvent event) {
        IpsModel model = (IpsModel)getIpsModel();
        IpsSrcFileContent content = model.getIpsSrcFileContent(getIpsSrcFile());
        if (content != null) {
            content.ipsObjectChanged(event);
        } else {
            model.ipsSrcFileContentHasChanged(event);
        }

    }

    private void checkExtProperty(String propertyId) {
        if (!isExtPropertyDefinitionAvailable(propertyId)) {
            throw new IllegalArgumentException("Extension property " + propertyId + " is not defined for type " //$NON-NLS-1$ //$NON-NLS-2$
                    + getClass().getName());
        }
    }

    private void initExtPropertiesIfNotDoneSoFar() {
        if (extPropertyValues == null) {
            extPropertyValues = new HashMap<String, Object>();
            Collection<IExtensionPropertyDefinition> properties = getExtensionPropertyDefinitions();
            for (IExtensionPropertyDefinition propertie : properties) {
                extPropertyValues.put(propertie.getPropertyId(), propertie.getDefaultValue(this));
            }
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element newElement = createElement(doc);
        propertiesToXml(newElement);
        extPropertiesToXml(newElement);
        partsToXml(doc, newElement);
        return newElement;
    }

    private void extPropertiesToXml(Element element) {
        Collection<IExtensionPropertyDefinition> propertyDefinitions = getExtensionPropertyDefinitions();
        if (propertyDefinitions.isEmpty() && invalidExtPropertyValues.isEmpty()) {
            return;
        }
        initExtPropertiesIfNotDoneSoFar();
        Document doc = element.getOwnerDocument();
        Element extPropertiesEl = doc.createElement(XML_EXT_PROPERTIES_ELEMENT);
        element.appendChild(extPropertiesEl);
        for (IExtensionPropertyDefinition propertyDefinition : propertyDefinitions) {
            extPropertyToXml(extPropertiesEl, propertyDefinition);
        }
        for (Entry<String, Node> entry : invalidExtPropertyValues.entrySet()) {
            if (!extPropertyValues.containsKey(entry.getKey())) {
                Node node = entry.getValue();
                Node importedNode = doc.importNode(node, true);
                extPropertiesEl.appendChild(importedNode);
            }
        }
        extPropertiesWithMissingDefinitionsToXml(extPropertiesEl, propertyDefinitions);
    }

    private void extPropertyToXml(Element extPropertiesEl, IExtensionPropertyDefinition propertyDefinition) {
        Element valueEl = extPropertiesEl.getOwnerDocument().createElement(IpsObjectPartContainer.XML_VALUE_ELEMENT);
        extPropertiesEl.appendChild(valueEl);
        String propertyId = propertyDefinition.getPropertyId();
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID, propertyId);
        Object value = extPropertyValues.get(propertyId);
        valueEl.setAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL, value == null ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
        if (value != null) {
            propertyDefinition.valueToXml(valueEl, value);
        }
    }

    /**
     * Writes all extension property values without {@link ExtensionPropertyDefinition} to XML. The
     * String value is written to XML without being modified.
     * 
     * @param extPropertiesEl the element the ext-property values should be added to
     * @param propertyDefinitions the list of available extension property definitions.
     */
    private void extPropertiesWithMissingDefinitionsToXml(Element extPropertiesEl,
            Collection<IExtensionPropertyDefinition> propertyDefinitions) {
        Map<String, Object> extpropMap = getExtPropertyValueMapForMissingDefinitions(propertyDefinitions);
        for (String propertyId : extpropMap.keySet()) {
            ExtensionPropertyDefinition propertyDefinition = new StringExtensionPropertyDefinition();
            propertyDefinition.setPropertyId(propertyId);
            extPropertyToXml(extPropertiesEl, propertyDefinition);
        }
    }

    private Map<String, Object> getExtPropertyValueMapForMissingDefinitions(Collection<IExtensionPropertyDefinition> propertyDefinitions) {
        Map<String, Object> extpropMap = new HashMap<String, Object>(extPropertyValues);
        for (IExtensionPropertyDefinition propertyDefinition : propertyDefinitions) {
            extpropMap.remove(propertyDefinition.getPropertyId());
        }
        return extpropMap;
    }

    /**
     * Is called from the toXml() method to create the xml element for this container.
     */
    protected abstract Element createElement(Document doc);

    /**
     * Transforms the parts this container contains to xml elements and adds them to the given xml
     * element.
     * 
     * @param doc xml document used to create new element.
     * 
     * @param element the element to which the part elements should be added.
     */
    private void partsToXml(Document doc, Element element) {
        IIpsElement[] children = getChildren();
        for (IIpsElement element2 : children) {
            IIpsObjectPart part = (IIpsObjectPart)element2;
            if (isPartSavedToXml(part)) {
                Element newPartElement = part.toXml(doc);
                element.appendChild(newPartElement);
            }
        }
    }

    /**
     * Returns whether the provided {@link IIpsObjectPart} should be persisted to XML.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The default implementation always returns true.
     * 
     * @param part the {@link IIpsObjectPart} in question to save
     */
    protected boolean isPartSavedToXml(IIpsObjectPart part) {
        return true;
    }

    /**
     * The method is called by the toXml() method, so that subclasses can store their properties in
     * the xml element passed as parameter.
     */
    protected abstract void propertiesToXml(Element element);

    @Override
    public void initFromXml(Element element) {
        initFromXml(element, null);
    }

    protected void initFromXml(Element element, String id) {
        initPropertiesFromXml(element, id);
        initPartContainersFromXml(element);
        initExtPropertiesFromXml(element);
    }

    /**
     * Add the given extension property value identified by the given property id. If the extension
     * property not exists as definitions then the property will be ignored.
     * 
     * @param propertyId id of the extension property
     * @param extPropertyValue extension property value
     */
    protected void addExtensionPropertyValue(String propertyId, String extPropertyValue) {
        initExtPropertiesIfNotDoneSoFar();
        Object value = null;
        if (extPropertyValue != null) {
            IExtensionPropertyDefinition property = getExtensionPropertyDefinition(propertyId);
            if (property == null) {
                return;
            }
            value = property.getValueFromString(extPropertyValue);
        }
        extPropertyValues.put(propertyId, value);
    }

    /**
     * The method is called by the initFromXml() method, so that subclasses can load their
     * properties from the xml element passed as parameter.
     * 
     * @param id The value for the id-property of the ips object part or null, if the id should be
     *            generated automatically (preferred).
     */
    protected abstract void initPropertiesFromXml(Element element, String id);

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     * 
     * @param containerEl The &lt;ExtensionProperties&gt; element.
     */
    private void initExtPropertiesFromXml(Element containerEl) {
        extPropertyValues = null;
        // to make sure that new extension properties are
        // initialized with their default.
        initExtPropertiesIfNotDoneSoFar();
        Element extPropertiesEl = XmlUtil.getFirstElement(containerEl, XML_EXT_PROPERTIES_ELEMENT);
        if (extPropertiesEl == null) {
            extPropertyValues = null;
            return;
        }
        NodeList nl = extPropertiesEl.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getNodeName().equals(IpsObjectPartContainer.XML_VALUE_ELEMENT)) {
                initExtPropertyFromXml((Element)node);
            }
        }
    }

    /**
     * The method is called by the initFromXml() method to retrieve the values of the extension
     * properties.
     */
    private void initExtPropertyFromXml(Element valueElement) {
        String propertyId = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_EXTPROPERTYID);
        Object value = null;
        String isNull = valueElement.getAttribute(IpsObjectPartContainer.XML_ATTRIBUTE_ISNULL);
        IExtensionPropertyDefinition property = getExtensionPropertyDefinition(propertyId);
        if (property == null) {
            IpsPlugin.log(new IpsStatus(IStatus.WARNING, "Extension property " + propertyId + " for " + this //$NON-NLS-1$ //$NON-NLS-2$
                    + " is unknown")); //$NON-NLS-1$
            invalidExtPropertyValues.put(propertyId, valueElement.cloneNode(true));
            return;
        }
        if (StringUtils.isEmpty(isNull) || !Boolean.valueOf(isNull).booleanValue()) {
            value = property.getValueFromXml(valueElement);
        }
        extPropertyValues.put(propertyId, value);
    }

    @Override
    public Collection<IExtensionPropertyDefinition> getExtensionPropertyDefinitions() {
        Map<String, IExtensionPropertyDefinition> properties = getIpsModel().getExtensionPropertyDefinitions(this);
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public IExtensionPropertyDefinition getExtensionPropertyDefinition(String propertyId) {
        return getIpsModel().getExtensionPropertyDefinitions(this).get(propertyId);
    }

    private void initPartContainersFromXml(Element element) {
        HashMap<String, IIpsObjectPart> idPartMap = createIdPartMap();
        Set<String> idSet = new HashSet<String>();
        reinitPartCollections();
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element partEl = (Element)item;
            if (partEl.getNodeName().equals(XML_EXT_PROPERTIES_ELEMENT)) {
                continue;
            }
            String id = partEl.getAttribute("id").trim(); //$NON-NLS-1$
            IIpsObjectPart part = idPartMap.get(id);
            if (part == null) {
                part = newPart(partEl, getNextPartId());
            } else {
                boolean added = addPart(part);
                if (!(added)) {
                    throw new IllegalArgumentException("Could not re-add part " + part); //$NON-NLS-1$
                }
            }
            // part might be null if the element does not represent a part!
            if (part != null) {
                part.initFromXml(partEl);
                if (!idSet.add(part.getId())) {
                    throw new RuntimeException("Duplicated Part-ID in Object " + part.getParent().getName() + ", ID: " //$NON-NLS-1$ //$NON-NLS-2$
                            + part.getId());
                }
            }
        }
        return;
    }

    private HashMap<String, IIpsObjectPart> createIdPartMap() {
        HashMap<String, IIpsObjectPart> map = new HashMap<String, IIpsObjectPart>();
        IIpsElement[] parts = getChildren();
        for (IIpsElement part2 : parts) {
            IIpsObjectPart part = (IIpsObjectPart)part2;
            map.put(part.getId(), part);
        }
        return map;
    }

    /**
     * This method is called during the process of initialization from XML. All collections that
     * hold references to {@link IIpsObjectPart}s need to be cleared. It might be extended by
     * subclasses by overriding the method {@link #reinitPartCollectionsThis()}.
     */
    protected final void reinitPartCollections() {
        labels.clear();
        descriptions.clear();
        reinitPartCollectionsThis();
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #reinitPartCollections()}.
     */
    protected abstract void reinitPartCollectionsThis();

    /**
     * Adds the given part to the container. Returns <tt>true</tt> if successfully added,
     * <tt>false</tt> otherwise.
     * <p>
     * This method is called during the process of initialization from XML when the part has been
     * part of the parent before the XML initialization and is still found in the XML file (the
     * part's id is found in the XML file). To avoid a StackOverflow it is important NOT to call the
     * {@link #objectHasChanged()} method after adding a part!
     * 
     * @param part The {@link IIpsObjectPart} to add to this container.
     */
    protected final boolean addPart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.add((ILabel)part);
            return true;

        } else if (part instanceof IDescription) {
            descriptions.add((IDescription)part);
            return true;
        }
        return addPartThis(part);
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #addPart(IIpsObjectPart)}.
     * <p>
     * The given part must be added to the correct collection.
     * <p>
     * This method is called by {@link #addPart(IIpsObjectPart)} which is called during xml
     * initialization. It is important NOT to call the {@link #objectHasChanged()} method after
     * adding a part!
     * 
     * @param part The {@link IIpsObjectPart} to add to this container.
     */
    protected abstract boolean addPartThis(IIpsObjectPart part);

    /**
     * Removes the given part from the container. Returns <tt>true</tt> if removed, <tt>false</tt>
     * otherwise.
     * <p>
     * Subclasses may extend this method by using the method {@link #removePartThis(IIpsObjectPart)}.
     * 
     * @param part The {@link IIpsObjectPart} to remove from this container.
     */
    protected final boolean removePart(IIpsObjectPart part) {
        if (part instanceof ILabel) {
            labels.remove(part);
            return true;

        } else if (part instanceof IDescription) {
            descriptions.remove(part);
            return true;
        }
        return removePartThis(part);
    }

    /**
     * Subclass implementation that can be used to extend the method
     * {@link #removePart(IIpsObjectPart)}.
     * 
     * @param part The {@link IIpsObjectPart} to remove from this container.
     */
    protected abstract boolean removePartThis(IIpsObjectPart part);

    /**
     * This method is called during the process of initialization from XML to create a new part
     * object for the given element with the given id.
     * 
     * @param xmlTag The XML tag that describes the part to create.
     * @param id The unique id for the new part.
     */
    protected final IIpsObjectPart newPart(Element xmlTag, String id) {
        String nodeName = xmlTag.getNodeName();
        if (nodeName.equals(ILabel.XML_TAG_NAME)) {
            if (this instanceof ILabeledElement) {
                return newLabel(id);
            } else {
                return null;
            }

        } else if (nodeName.equals(IDescription.XML_TAG_NAME)) {
            if (this instanceof IDescribedElement) {
                return newDescription(id);
            } else {
                return null;
            }
        }
        return newPartThis(xmlTag, id);
    }

    /**
     * Subclass implementation that must create and return the right part based on the XML element.
     * <p>
     * <strong>Note:</strong> It is <strong>NOT</strong> necessary to fully initialize the part,
     * this is done later by the caller calling initFromXml(). Also it is NOT <strong>NOT</strong>
     * necessary to notify any change listener about the newly added part!
     * <p>
     * Should return <tt>null</tt> if the XML tag is unknown.
     * 
     * @param xmlTag The XML tag that describes the part to create.
     * @param id The unique id for the new part.
     */
    protected abstract IIpsObjectPart newPartThis(Element xmlTag, String id);

    /**
     * Creates a new {@link IIpsObjectPart} of the given type. If the type is not supported,
     * <tt>null</tt> is returned.
     * 
     * @param partType The published interface of the IPS object part that should be created.
     */
    @SuppressWarnings("unchecked")
    // not type safe because newPartThis is not type safe
    public final <T extends IIpsObjectPart> T newPart(Class<T> partType) {
        if (partType == Label.class) {
            T newLabel = (T)newLabel();
            return newLabel;

        } else if (partType == Description.class) {
            T newDescription = (T)newDescription();
            return newDescription;
        }
        T newPartThis = (T)newPartThis(partType);
        return newPartThis;
    }

    /**
     * Subclass implementation that must create and return the right part based on the given
     * published interface class.
     * <p>
     * Should return <tt>null</tt> if the type is unknown.
     * <p>
     * This class is not type safe because it was to late to implement generics correctly. However
     * the implementer needs to ensure that the returned object could safely casted to
     * <code>partType</code>.
     * 
     * @param partType The published interface of the IPS object part that should be created.
     */
    protected abstract IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType);

    @Override
    public MessageList validate(IIpsProject ipsProject) throws CoreException {
        if (isHistoricPartContainer()) {
            return new MessageList();
        }
        MessageList result = beforeValidateThis();
        if (result != null) {
            return result;
        }

        result = new MessageList();

        // Obtain the project properties via the IPS model as it provides caching
        IIpsProjectProperties properties = ((IpsModel)getIpsModel())
                .getIpsProjectProperties((IpsProject)getIpsProject());
        int languageCount = properties.getSupportedLanguages().size();
        if (this instanceof IDescribedElement) {
            validateDescriptionCount(result, languageCount);
        }
        if (this instanceof ILabeledElement) {
            validateLabelCount(result, languageCount);
        }

        validateThis(result, ipsProject);
        execCustomValidations(result, ipsProject);
        afterValidateThis(result, ipsProject);
        return result;
    }

    private void validateDescriptionCount(MessageList result, int languageCount) {
        int descriptionCount = descriptions.size();
        if (descriptionCount != languageCount) {
            String text = NLS.bind(Messages.IpsObjectPartContainer_msgInvalidDescriptionCount, descriptionCount,
                    languageCount);
            Message message = Message.newWarning(IIpsObjectPartContainer.MSGCODE_INVALID_DESCRIPTION_COUNT, text, this);
            result.add(message);
        }
    }

    private void validateLabelCount(MessageList result, int languageCount) {
        int labelCount = labels.size();
        if (labelCount != languageCount) {
            String text = NLS.bind(Messages.IpsObjectPartContainer_msgInvalidLabelCount, labelCount, languageCount);
            Message message = Message.newWarning(IIpsObjectPartContainer.MSGCODE_INVALID_LABEL_COUNT, text, this);
            result.add(message);
        }
    }

    private void execCustomValidations(MessageList result, IIpsProject ipsProject) throws CoreException {
        Class<IpsObjectPartContainer> thisClass = (Class<IpsObjectPartContainer>)getClass();
        Set<ICustomValidation<IpsObjectPartContainer>> customValidations = getIpsModel().getCustomModelExtensions()
                .getCustomValidations(thisClass);
        for (ICustomValidation<IpsObjectPartContainer> validation : customValidations) {
            result.add(validation.validate(this, ipsProject));
        }
    }

    /**
     * Before validation method. Perform operations which will be executed before this object part
     * container will be validated. Returns the cached validation result for the given container or
     * <code>null</code> if the cache does not contain a result for the container.
     */
    protected MessageList beforeValidateThis() {
        MessageList result = getValidationCache().getResult(this);
        if (result != null) {
            if (IpsModel.TRACE_VALIDATION) {
                System.out.println("Validation of " + this + ": Got result from cache."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return result;
        }

        if (IpsModel.TRACE_VALIDATION) {
            validationStartTime = System.currentTimeMillis();
            System.out.println("Validation of " + this + ": Started."); //$NON-NLS-1$//$NON-NLS-2$
        }
        return result;
    }

    /**
     * After validation method. Perform operations which will be executed after validation of this
     * object part container.
     */
    protected void afterValidateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        validateExtensionProperties(result);
        validateChildren(result, ipsProject);
        if (IpsModel.TRACE_VALIDATION) {
            System.out.println("Validation of " + this + ": Finished, took " //$NON-NLS-1$ //$NON-NLS-2$
                    + (System.currentTimeMillis() - validationStartTime) + "ms."); //$NON-NLS-1$
            validationStartTime = -1;
        }
        getValidationCache().putResult(this, result);
    }

    private ValidationResultCache getValidationCache() {
        IpsModel model = (IpsModel)getIpsModel();
        ValidationResultCache cache = model.getValidationResultCache();
        return cache;
    }

    /**
     * Validates part container's children.
     */
    protected void validateChildren(MessageList result, IIpsProject ipsProject) throws CoreException {
        IIpsElement[] children = getChildren();
        for (IIpsElement element : children) {
            MessageList childResult = ((IpsObjectPartContainer)element).validate(ipsProject);
            result.add(childResult);
        }
    }

    /**
     * Returns true if this <code>IpsObjectPartContainer</code> is part of an
     * <code>IIpsSrcFile</code> that is marked as historic. If no srcfile can be found, false is
     * returned.
     * 
     * @return True only if the parent srcfile is historic, false otherwise.
     */
    private boolean isHistoricPartContainer() {
        IIpsElement container = this;
        while (container != null) {
            if (container instanceof IIpsSrcFile && ((IIpsSrcFile)container).isHistoric()) {
                return true;
            }
            container = container.getParent();
        }
        return false;
    }

    /**
     * Validates the object and reports invalid states by adding validation messages to the list.
     * This is an application of the collecting parameter pattern.
     * 
     * @param list The message list containing all validation messages - if you overwrite this
     *            method you must add your validation messages to this list.
     * @param ipsProject The context IPS project. The validation might be called from a different
     *            IPS project than the actual instance of this validatable belongs to. In this case
     *            it is necessary to use the IPS project of the caller for finder-methods that are
     *            used within the implementation of this method.
     * 
     * @throws CoreException Subclasses may wrap any occurring exceptions into a CoreException and
     *             propagate it trough this method.
     * @throws NullPointerException if list is <code>null</code>.
     */
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        return;
    }

    /**
     * Validates the extension property values.
     * 
     * @param ml The message list to which messages generated during the validation are added.
     * 
     * @throws CoreException if an error occurs while validation the extension properties.
     */
    protected void validateExtensionProperties(MessageList ml) throws CoreException {
        Collection<IExtensionPropertyDefinition> properties = getExtensionPropertyDefinitions();
        for (IExtensionPropertyDefinition propertie : properties) {
            Object value = getExtPropertyValue(propertie.getPropertyId());
            MessageList newList = propertie.validate(this, value);
            if (newList != null) {
                ml.add(newList);
            }
        }
    }

    @Override
    public Memento newMemento() {
        Document doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        return new XmlMemento(this, toXml(doc));
    }

    @Override
    public void setState(Memento memento) {
        if (!equals(memento.getOriginator())) {
            throw new IllegalArgumentException("Memento " + memento + " wasn't created by " + this); //$NON-NLS-1$ //$NON-NLS-2$
        }

        initFromXml(((XmlMemento)memento).getState());
        objectHasChanged();
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Helper to easily add a new {@link IDependencyDetail} to the map.
     * 
     * @param details The map of dependencies to the list of details.
     * @param dependency The dependency to add the details for
     * @param part The details part
     * @param propertyName The details property name
     */
    protected void addDetails(Map<IDependency, List<IDependencyDetail>> details,
            IDependency dependency,
            IIpsObjectPartContainer part,
            String propertyName) {
        DependencyDetail dependencyDetail = new DependencyDetail(part, propertyName);
        addDetails(details, dependency, dependencyDetail);
    }

    /**
     * Helper to easily add a {@link IDependencyDetail} to the list of dependencies.
     * 
     * @param details The map of dependencies to the list of details.
     * @param dependency The dependency to add the details for
     * @param dependencyDetail The {@link IDependencyDetail} that should be added
     */
    protected void addDetails(Map<IDependency, List<IDependencyDetail>> details,
            IDependency dependency,
            DependencyDetail dependencyDetail) {
        if (details == null) {
            return;
        }
        List<IDependencyDetail> detailList = details.get(dependency);
        if (detailList == null) {
            detailList = new ArrayList<IDependencyDetail>();
            details.put(dependency, detailList);
        }
        detailList.add(dependencyDetail);
    }

    private ILabel newLabel(String id) {
        ILabel newLabel = new Label(this, id);
        labels.add(newLabel);
        return newLabel;
    }

    private IDescription newDescription(String id) {
        IDescription newDescription = new Description(this, id);
        descriptions.add(newDescription);
        return newDescription;
    }

    /**
     * @see ILabeledElement#getLabel(Locale)
     */
    public ILabel getLabel(Locale locale) {
        ArgumentCheck.notNull(locale);

        for (ILabel label : labels) {
            Locale labelLocale = label.getLocale();
            if (labelLocale == null) {
                continue;
            }
            if (locale.getLanguage().equals(labelLocale.getLanguage())) {
                return label;
            }
        }
        return null;
    }

    /**
     * @see ILabeledElement#getLabels()
     */
    public List<ILabel> getLabels() {
        List<ILabel> orderedLabels = new ArrayList<ILabel>(labels.size());
        for (ISupportedLanguage language : getIpsProject().getReadOnlyProperties().getSupportedLanguages()) {
            Locale locale = language.getLocale();
            if (locale == null) {
                continue;
            }
            ILabel label = getLabel(locale);
            if (label != null) {
                orderedLabels.add(label);
            }
        }
        for (ILabel label : labels) {
            if (!(orderedLabels.contains(label))) {
                orderedLabels.add(label);
            }
        }
        return orderedLabels;
    }

    /**
     * @see ILabeledElement#newLabel()
     */
    public ILabel newLabel() {
        return newLabel(getNextPartId());
    }

    /**
     * @see ILabeledElement#isPluralLabelSupported()
     */
    public boolean isPluralLabelSupported() {
        return false;
    }

    /**
     * @see ILabeledElement#getLabelValue(Locale)
     */
    public String getLabelValue(Locale locale) {
        ILabel label = getLabel(locale);
        return label == null ? null : label.getValue();
    }

    /**
     * @see ILabeledElement#getPluralLabelValue(Locale)
     */
    public String getPluralLabelValue(Locale locale) {
        ILabel label = getLabel(locale);
        return label == null ? null : label.getPluralValue();
    }

    /**
     * @see ILabeledElement#setLabelValue(Locale, String)
     */
    public void setLabelValue(Locale locale, String value) {
        ILabel label = getLabel(locale);
        if (label == null) {
            throw new IllegalArgumentException("There is no label with the locale '" + locale + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
        label.setValue(value);
    }

    /**
     * @see ILabeledElement#setPluralLabelValue(Locale, String)
     */
    public void setPluralLabelValue(Locale locale, String pluralValue) {
        ILabel label = getLabel(locale);
        if (label == null) {
            throw new IllegalArgumentException("There is no label with the locale '" + locale + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
        label.setPluralValue(pluralValue);
    }

    /**
     * @see IDescribedElement#getDescription(Locale)
     */
    public IDescription getDescription(Locale locale) {
        ArgumentCheck.notNull(locale);

        for (IDescription description : descriptions) {
            Locale descriptionLocale = description.getLocale();
            if (descriptionLocale == null) {
                continue;
            }
            if (locale.getLanguage().equals(descriptionLocale.getLanguage())) {
                return description;
            }
        }
        return null;
    }

    /**
     * @see IDescribedElement#getDescriptionText(Locale)
     */
    public String getDescriptionText(Locale locale) {
        IDescription description = getDescription(locale);
        return description == null ? "" : description.getText(); //$NON-NLS-1$
    }

    /**
     * @see IDescribedElement#getDescriptions()
     */
    public List<IDescription> getDescriptions() {
        List<IDescription> orderedDescriptions = new ArrayList<IDescription>(descriptions.size());
        for (ISupportedLanguage language : getIpsProject().getReadOnlyProperties().getSupportedLanguages()) {
            Locale locale = language.getLocale();
            if (locale == null) {
                continue;
            }
            IDescription description = getDescription(locale);
            if (description != null) {
                orderedDescriptions.add(description);
            }
        }
        for (IDescription description : descriptions) {
            if (!(orderedDescriptions.contains(description))) {
                orderedDescriptions.add(description);
            }
        }
        return orderedDescriptions;
    }

    /**
     * @see IDescribedElement#newDescription()
     */
    public IDescription newDescription() {
        return newDescription(getNextPartId());
    }

    /**
     * @see IDescribedElement#setDescriptionText(Locale, String)
     */
    public void setDescriptionText(Locale locale, String text) {
        IDescription description = getDescription(locale);
        if (description == null) {
            throw new IllegalArgumentException("There is no description with the locale '" + locale + "'."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        description.setText(text);
    }

    // Deprecated since 3.1
    @Deprecated
    @Override
    public String getDescription() {
        String description = ""; //$NON-NLS-1$
        if (this instanceof IDescribedElement) {
            description = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription((IDescribedElement)this);
        }
        return description;
    }

    // Deprecated since 3.1
    @Deprecated
    @Override
    public void setDescription(String newDescription) {
        if (!(isDescriptionChangable())) {
            throw new UnsupportedOperationException();
        }
        if (newDescription == null) {
            // Throw exception as specified by the contract.
            throw new IllegalArgumentException();
        }

        List<IDescription> descriptionList = getDescriptions();
        if (descriptionList.size() == 0) {
            return;
        }
        IDescription firstDescription = descriptionList.get(0);
        firstDescription.setText(newDescription);
    }

    @Deprecated
    @Override
    public final boolean isDescriptionChangable() {
        return this instanceof IDescribedElement;
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses to
     * provide the correct caption.
     */
    @Override
    public String getCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);
        return ""; //$NON-NLS-1$
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses to
     * provide the correct plural caption.
     */
    @Override
    public String getPluralCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);
        return ""; //$NON-NLS-1$
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses if
     * they are able to provide a reasonable last resort caption.
     */
    @Override
    public String getLastResortCaption() {
        return ""; //$NON-NLS-1$
    }

    /**
     * This implementation always returns an empty string and should be overridden by subclasses if
     * they are able to provide a reasonable last resort plural caption.
     */
    @Override
    public String getLastResortPluralCaption() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public void copyFrom(IIpsObjectPartContainer source) {
        ArgumentCheck.isTrue(getClass().equals(source.getClass()));

        Element xmlElement = source.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
        initFromXml(xmlElement);
    }

}
