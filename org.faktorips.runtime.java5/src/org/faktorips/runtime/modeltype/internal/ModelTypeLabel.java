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

package org.faktorips.runtime.modeltype.internal;

import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.modeltype.IModelTypeLabel;

/**
 * @author Alexander Weickmann
 */
public class ModelTypeLabel extends AbstractModelElement implements IModelTypeLabel {

    private ModelType modelType;

    private Locale locale;

    private String value;

    private String pluralValue;

    public ModelTypeLabel(ModelType modelType) {
        super(modelType.getAbstractRepository());
        this.modelType = modelType;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    public String getPluralValue() {
        return pluralValue;
    }

    @Override
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("locale")) {
                String localeCode = parser.getAttributeValue(i);
                locale = localeCode.equals("") ? null : new Locale(localeCode);
            } else if (parser.getAttributeLocalName(i).equals("value")) {
                value = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals("pluralValue")) {
                pluralValue = parser.getAttributeValue(i);
            }
        }
    }

}