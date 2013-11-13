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

package org.faktorips.devtools.core.ui.inputformat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.events.VerifyEvent;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.EnumTypeDisplay;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;

public class EnumDatatypeInputFormat extends AbstractInputFormat<String> {

    private final EnumDatatype enumDatatype;

    private final IpsPreferences ipsPreferences;

    public EnumDatatypeInputFormat(EnumDatatype enumDatatype, IpsPreferences ipsPreferences) {
        this.enumDatatype = enumDatatype;
        this.ipsPreferences = ipsPreferences;
    }

    public static EnumDatatypeInputFormat newInstance(EnumDatatype enumDatatype) {
        return new EnumDatatypeInputFormat(enumDatatype, IpsPlugin.getDefault().getIpsPreferences());
    }

    @Override
    protected String parseInternal(String stringToBeparsed) {
        String parsedAsId = parseValueId(stringToBeparsed);
        if (parsedAsId != null) {
            return parsedAsId;
        }
        String parsedAsName = parseValueName(stringToBeparsed);
        if (parsedAsName != null) {
            return parsedAsName;
        }
        String parsedAsNameAndId = parseValueNameAndID(stringToBeparsed);
        EnumTypeDisplay enumTypeDisplay = ipsPreferences.getEnumTypeDisplay();
        if (EnumTypeDisplay.NAME_AND_ID.equals(enumTypeDisplay)) {
            if (parsedAsNameAndId != null) {
                return parsedAsNameAndId;
            }
        }
        return stringToBeparsed;
    }

    protected String parseValueId(String stringToBeparsed) {
        if (enumDatatype.isParsable(stringToBeparsed)) {
            return stringToBeparsed;
        } else {
            return null;
        }
    }

    protected String parseValueName(String stringToBeparsed) {
        String[] allValueIds = enumDatatype.getAllValueIds(false);
        for (String valueId : allValueIds) {
            String valueName = enumDatatype.getValueName(valueId);
            if (stringToBeparsed.equals(valueName)) {
                return valueId;
            }
        }
        return null;
    }

    protected String parseValueNameAndID(String stringToBeparsed) {
        Pattern pattern = Pattern.compile("(?<=\\()(.*?)(?=\\))"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(stringToBeparsed);
        int groupCount = matcher.groupCount();
        if (groupCount > 0 && matcher.find()) {
            String id = matcher.group(groupCount - 1);
            return parseValueId(id);
        } else {
            return null;
        }
    }

    @Override
    protected String formatInternal(String value) {
        return IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormatter().formatValue(enumDatatype, value);
    }

    @Override
    protected void verifyInternal(VerifyEvent e, String resultingText) {
        // no verify yet
    }

    @Override
    protected void initFormat(Locale locale) {
        // do nothing
    }

}