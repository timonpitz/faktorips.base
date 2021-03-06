/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.valueset;

import static org.faktorips.devtools.core.model.valueset.IValueSet.MSGCODE_UNKNOWN_DATATYPE;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;

public class StringLengthValueSetValidator extends AbstractValueSetValidator<StringLengthValueSet> {

    public StringLengthValueSetValidator(StringLengthValueSet valueSet, IValueSetOwner owner, ValueDatatype datatype) {
        super(valueSet, owner, datatype);
    }

    @Override
    public MessageList validate() {
        MessageList messages = new MessageList();

        String maxLengthValue = getValueSet().getMaximumLength();

        if (getDatatype() == null) {
            messages.newError(MSGCODE_UNKNOWN_DATATYPE, Messages.EnumValueSet_msgDatatypeUnknown,
                    getInvalidObjectProperty(StringLengthValueSet.PROPERTY_MAXIMUMLENGTH));
            return messages;
        }

        if (!ValidationUtils.checkParsable(Datatype.INTEGER, maxLengthValue, getValueSet(),
                StringLengthValueSet.PROPERTY_MAXIMUMLENGTH,
                messages)) {
            return messages;
        } else {
            Integer maxLength = maxLengthValue == null ? null : Integer.parseInt(maxLengthValue);
            if (maxLength != null && maxLength < 0) {
                messages.newError(StringLengthValueSet.MSGCODE_NEGATIVE_VALUE,
                        Messages.StringLength_msgNegativeValue, StringLengthValueSet.PROPERTY_MAXIMUMLENGTH);
            }
        }

        return messages;
    }

    private ObjectProperty getInvalidObjectProperty(String propertyName) {
        return new ObjectProperty(getValueSet(), propertyName);
    }

}
