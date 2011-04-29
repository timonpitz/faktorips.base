/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.changelistener;

import org.faktorips.codegen.JavaCodeFragmentBuilder;

/**
 * Interface for change listener support builders allowing the implementation of different event
 * listener mechanisms.
 * 
 * @author Daniel Hohenberger
 */
public interface IChangeListenerSupportBuilder {

    /**
     * Generates code for the notification of change listeners that is executed before the change
     * occurs.
     * 
     * @param methodsBuilder the builder used for writing the code.
     * @param eventType the type of event.
     * @param fieldType the class name of the field's type.
     * @param fieldName the name of the changed property.
     * @param paramName the name of the parameter used to change the property.
     */
    public void generateChangeListenerSupportBeforeChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String fieldType,
            String fieldName,
            String paramName,
            String fieldNameConstant);

    /**
     * Generates code for the notification of change listeners that is executed after the change
     * occurs.
     * 
     * @param methodsBuilder the builder used for writing the code.
     * @param eventType the type of event.
     * @param fieldType the class name of the field's type.
     * @param fieldName the name of the changed property.
     * @param paramName the name of the parameter used to change the property.
     * @param fieldNameConstant the name of the constant used to hold the fieldName at runtime.
     */
    public void generateChangeListenerSupportAfterChange(JavaCodeFragmentBuilder methodsBuilder,
            ChangeEventType eventType,
            String fieldType,
            String fieldName,
            String paramName,
            String fieldNameConstant);

    /**
     * Generates the method that is called by the code generated by
     * <code>generateChangeListenerSupport</code> and other utility methods, e.g. for registering
     * change listeners. Here filtering and propagation of events can happen.
     * 
     * @param methodBuilder the builder used for writing the code.
     * @param parentObjectFieldNames the field names of all parent objects (used for propagation).
     * @param generateParentNotification <code>true</code> if the add-/remove- and hasListener
     *            Method should be created or not. These methods should only be generated if the
     *            current type has no supertype, means this type is the supertype
     */
    public void generateChangeListenerMethods(JavaCodeFragmentBuilder methodBuilder,
            String[] parentObjectFieldNames,
            boolean generateParentNotification);

    /**
     * Generates the constants the change listener methods need.
     * 
     * @param builder the builder used for writing the code.
     */
    public void generateChangeListenerConstants(JavaCodeFragmentBuilder builder);

    /**
     * Returns the name of the interface classes offering notification support must implement or
     * {@code null} if no interface is required.
     */
    public String getNotificationSupportInterfaceName();
}
