/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsExtensionProperty;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * 
 * @author Daniel Hohenberger
 */
public abstract class AbstractModelElement implements IModelElement {

    private final String name;

    private final Map<String, Object> extPropertyValues;

    public AbstractModelElement(String name, IpsExtensionProperties extensionProperties) {
        this.name = name;
        extPropertyValues = initExtensionPropertyMap(extensionProperties);
    }

    private Map<String, Object> initExtensionPropertyMap(IpsExtensionProperties extensionPropertiesAnnotation) {
        Map<String, Object> result = Collections.emptyMap();
        if (extensionPropertiesAnnotation != null) {
            IpsExtensionProperty[] extensionProperties = extensionPropertiesAnnotation.value();
            result = new LinkedHashMap<String, Object>(extensionProperties.length, 1f);
            for (IpsExtensionProperty ipsExtensionProperty : extensionProperties) {
                result.put(ipsExtensionProperty.id(), initValue(ipsExtensionProperty));
            }
        }
        return result;
    }

    private Object initValue(IpsExtensionProperty ipsExtensionProperty) {
        if (ipsExtensionProperty.isNull()) {
            return null;
        } else {
            return ipsExtensionProperty.value();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLabel(Locale locale) {
        return getDocumentation(locale, DocumentationType.LABEL, getName());
    }

    @Override
    public String getDescription(Locale locale) {
        return getDocumentation(locale, DocumentationType.DESCRIPTION, IpsStringUtils.EMPTY);
    }

    private String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(getTypeName(), getName());
    }

    protected String getDocumentation(Locale locale, DocumentationType type, String fallback) {
        MessagesHelper messageHelper = getMessageHelper();
        if (messageHelper != null) {
            return messageHelper.getMessageOr(getMessageKey(type), locale, fallback);
        } else {
            return fallback;
        }
    }

    protected abstract MessagesHelper getMessageHelper();

    protected MessagesHelper createMessageHelper(IpsDocumented documentedAnnotation, ClassLoader classLoader) {
        if (documentedAnnotation != null) {
            String documentationResourceBundle = documentedAnnotation.bundleName();
            Locale defaultLocale = new Locale(documentedAnnotation.defaultLocale());
            return new MessagesHelper(documentationResourceBundle, classLoader, defaultLocale);
        } else {
            return null;
        }
    }

    protected abstract String getTypeName();

    @Override
    public Object getExtensionPropertyValue(String propertyId) {
        if (extPropertyValues == null) {
            return null;
        }
        return extPropertyValues.get(propertyId);
    }

    @Override
    public Set<String> getExtensionPropertyIds() {
        if (extPropertyValues == null) {
            return new HashSet<String>(0);
        }
        return extPropertyValues.keySet();
    }
}
