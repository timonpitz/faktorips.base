/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Locale;

/**
 * A validation context is provided to the validate() method generated by Faktor-IPS. By means of
 * the validation context the caller can provide additional information to the validate method like
 * for example the business context in which the validation is to execute.
 * 
 * @author Peter Erzberger
 */
public interface IValidationContext {

    /**
     * Returns the Locale that is to use for the creation of validation messages.
     */
    public Locale getLocale();

    /**
     * Returns the value for property with the specified name.
     */
    public Object getValue(String propertyName);
}