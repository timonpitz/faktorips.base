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

package org.faktorips.datatype.classtypes;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueClassDatatype;

/**
 * Datatype for <code>Integer</code>.
 * 
 * @author Jan Ortmann
 */
public class IntegerDatatype extends ValueClassDatatype implements NumericDatatype {

    public IntegerDatatype() {
        super(Integer.class);
    }

    public IntegerDatatype(String name) {
        super(Integer.class, name);
    }

    @Override
    public Object getValue(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        return Integer.valueOf(s);
    }

    public boolean supportsCompare() {
        return true;
    }

    public String subtract(String minuend, String subtrahend) {
        if (minuend == null || subtrahend == null) {
            throw new NullPointerException("Minuend and subtrahend both can not be null."); //$NON-NLS-1$
        }

        int result = ((Integer)getValue(minuend)).intValue() - ((Integer)getValue(subtrahend)).intValue();
        return Integer.toString(result);
    }

    public boolean divisibleWithoutRemainder(String dividend, String divisor) {
        if (dividend == null || divisor == null) {
            throw new NullPointerException("dividend and divisor both can not be null."); //$NON-NLS-1$
        }
        Integer intA = (Integer)getValue(dividend);
        Integer intB = (Integer)getValue(divisor);

        if (intA == null) {
            throw new NumberFormatException("The dividend '" + dividend + "' can not be parsed to an Integer"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (intB == null) {
            throw new NumberFormatException("The divisor '" + divisor + "' can not be parsed to an Integer"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        int a = intA.intValue();
        int b = intB.intValue();

        return b == 0 ? false : a % b == 0;
    }

    public boolean hasDecimalPlaces() {
        return false;
    }

}