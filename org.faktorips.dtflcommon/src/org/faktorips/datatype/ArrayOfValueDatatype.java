/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import org.apache.commons.lang.ObjectUtils;
import org.faktorips.util.ArgumentCheck;

/**
 * Datatype representing an array of primitive Datatypes with a dimension.
 * 
 * @author Jan Ortmann
 */
public class ArrayOfValueDatatype extends AbstractDatatype implements ValueDatatype {

    /** The primitive datatype. */
    private Datatype datatype;

    /** The dimension of the array. */
    private int dimension;

    /**
     * Constructs a new array datatype based on the given underlying datatype and the dimension.
     */
    public ArrayOfValueDatatype(Datatype datatype, int dimension) {
        super();
        ArgumentCheck.notNull(datatype);
        this.datatype = datatype;
        this.dimension = dimension;
    }

    /**
     * Returns the number of dimensions specified in the given datatypeName.
     * <p>
     * Examples:<br>
     * "Money" specifies 0 dimensions. "Money[]" specifies 1 dimension. "Money[][]" specifies 2
     * dimensions.
     */
    public static final int getDimension(String datatypeName) {
        if (datatypeName == null) {
            return 0;
        }
        int dimension = 0;
        while (datatypeName.endsWith("[]")) { //$NON-NLS-1$
            dimension++;
            datatypeName = datatypeName.substring(0, datatypeName.length() - 2);
        }
        return dimension;
    }

    /**
     * Returns the basic datatype name specified in the given datatypeName.
     * <p>
     * Examples:<br>
     * "Money" specifies basic datatype Money. "Money[]" specifies basic datatype Money. "Money[][]"
     * specifies basic datatype Money.
     */
    public static final String getBasicDatatypeName(String datatypeName) {
        while (datatypeName.endsWith("[]")) { //$NON-NLS-1$
            datatypeName = datatypeName.substring(0, datatypeName.length() - 2);
        }
        return datatypeName;
    }

    /**
     * Returns if the provided string represents an ArrayDatatype.
     */
    public static final boolean isArrayDatatype(String datatypeName) {
        return getDimension(datatypeName) != 0;
    }

    /**
     * Returns the array datatype's basic datatype. E.g. for an array of Money values,
     * <code>Datatype.MONEY</code> is the basic datatype.
     */
    public Datatype getBasicDatatype() {
        return datatype;
    }

    /**
     * Returns the array's dimension.
     */
    public int getDimension() {
        return dimension;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String getName() {
        StringBuffer buffer = new StringBuffer(datatype.getName());
        for (int i = 0; i < dimension; i++) {
            buffer.append("[]"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    @Override
    public String getQualifiedName() {
        StringBuffer buffer = new StringBuffer(datatype.getQualifiedName());
        for (int i = 0; i < dimension; i++) {
            buffer.append("[]"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public boolean isValueDatatype() {
        return true;
    }

    @Override
    public String getJavaClassName() {
        StringBuffer buffer = new StringBuffer(datatype.getJavaClassName());
        for (int i = 0; i < dimension; i++) {
            buffer.append("[]"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    @Override
    public ValueDatatype getWrapperType() {
        return null;
    }

    /**
     * Null is parsable. Other values are not supported yet. {@inheritDoc}
     */
    @Override
    public boolean isParsable(String value) {
        if (value == null) {
            return true;
        }
        // parsing of array value datatypes is not supported yet.
        return false;
    }

    /**
     * If the value is null, null will be returned. Other values are not supported yet.
     */
    @Override
    public Object getValue(String value) {
        if (value == null) {
            return null;
        }
        throw new RuntimeException("No supported yet."); //$NON-NLS-1$
    }

    /**
     * If the value is null, null will be returned. Other values are not supported yet.
     */
    public String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        throw new RuntimeException("No supported yet."); //$NON-NLS-1$
    }

    @Override
    public boolean isNull(String value) {
        return value == null;
    }

    @Override
    public boolean supportsCompare() {
        if (datatype.isValueDatatype() && ((ValueDatatype)datatype).supportsCompare()) {
            return true;
        }
        return false;
    }

    @Override
    public int compare(String valueA, String valueB) {
        if (!supportsCompare()) {
            throw new UnsupportedOperationException("The datatype " + datatype.getQualifiedName() //$NON-NLS-1$
                    + " does not support comparison for values."); //$NON-NLS-1$
        }

        return ((ValueDatatype)datatype).compare(valueA, valueB);
    }

    @Override
    public boolean areValuesEqual(String valueA, String valueB) {
        if (datatype.isValueDatatype()) {
            return ((ValueDatatype)datatype).areValuesEqual(valueA, valueB);
        }
        return ObjectUtils.equals(valueA, valueB);
    }

}
