/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.AbstractValueHolder;
import org.faktorips.devtools.core.model.Validatable;
import org.faktorips.devtools.core.model.XmlSupport;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.util.message.MessageList;

/**
 * This is the public interface for all value holders as they are used by {@link IAttributeValue}.
 * It is recommended to use the {@link AbstractValueHolder} class for implementing this interface.
 * <p>
 * A value holder is a child of an {@link IIpsObjectPart} for example an {@link IAttributeValue}.
 * Any changes within the value holder have to be propagated to this part.
 * 
 * @author dirmeier
 * @since 3.7
 */
public interface IValueHolder<T> extends XmlSupport, Validatable, Comparable<IValueHolder<T>> {

    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Returning the {@link IIpsObjectPart} of which this value holder is a child of. Every value
     * holder need to have a parent {@link IIpsObjectPart}. If anything within the value holder
     * changes, the change event is propagated to this part.
     * 
     * @return The parent {@link IIpsObjectPart}
     */
    public IIpsObjectPart getParent();

    /**
     * Returning a string representation of the value.
     * 
     * @return a string representation of this part.
     */
    public String getStringValue();

    /**
     * Setting a String representation of the value.
     * <p>
     * This method was introduced as deprecated method to handle existing implementations for
     * example for default values that are stored as String instead of value holders. You should not
     * use this method because the it is not specified how the implementation handles the given
     * string value. Especially it is not specified that {@link #getStringValue()} would return the
     * same string as previously set by this method.#
     * 
     * @param value The value you want to set
     * @deprecated Use {@link #setValue(Object)}
     */
    @Deprecated
    public void setStringValue(String value);

    /**
     * Returns the value of this value holder. The type of the value depends on the generic type T.
     * 
     * @return The current value stored in this value holder.
     */
    public T getValue();

    /**
     * Setting a new value for this value holder. The type of the value must match the generic type
     * T.
     * <p>
     * Setting a new value have to perform a change event on the parent.
     * 
     * @param value The value that should be set as current value in this holder.
     */
    public void setValue(T value);

    /**
     * Returns <code>true</code>, if the value is <code>null</code> otherwise <code>false</code>. It
     * depends on the specific ValueHolder.
     * 
     * @return boolean <code>true</code> if the value is <code>null</code>
     */
    boolean isNullValue();

    /**
     * The ValueType describe the kind of value used in this value holder. The different kinds are
     * described in the {@link ValueType}. The reason for {@link ValueType} is to distinguish the
     * kind of {@link IValue}.
     */
    ValueType getValueType();

    /**
     * Basically there are two different kinds of value holder: multi value holder and single value
     * holder. This method returns <code>true</code> if this value holder is a multi value holder.
     * It does not say anything about the concrete implementation so do not use for instance-of
     * check!
     * 
     * @return Returns <code>true</code> if the value holder has multiple values
     */
    public boolean isMultiValue();

    /**
     * Validates the value holder and uses the given parent instead of {@link #getParent()}.
     * <p>
     * This is needed to validate a value holder from a template in product component based on that
     * template, too.
     */
    public MessageList validate(IIpsProject ipsProject, IIpsObjectPart parent) throws CoreException;
}