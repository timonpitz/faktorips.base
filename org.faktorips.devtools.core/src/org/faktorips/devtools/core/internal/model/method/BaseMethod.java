/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.method;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObjectPart;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IBaseMethod}
 * 
 * @author frank
 */
public class BaseMethod extends BaseIpsObjectPart implements IBaseMethod {

    public static final String XML_ELEMENT_NAME = "Method"; //$NON-NLS-1$

    private IpsObjectPartCollection<IParameter> parameters = new IpsObjectPartCollection<IParameter>(this,
            Parameter.class, IParameter.class, Parameter.TAG_NAME);

    private String datatype = "void"; //$NON-NLS-1$

    public BaseMethod(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, name);
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDatatype(String newDatatype) {
        String oldDatatype = getDatatype();
        this.datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype, PROPERTY_DATATYPE);
    }

    @Override
    public IParameter newParameter() {
        return parameters.newPart();
    }

    @Override
    public IParameter newParameter(String datatype, String name) {
        IParameter param = newParameter();
        param.setDatatype(datatype);
        param.setName(name);
        return param;
    }

    @Override
    public int getNumOfParameters() {
        return parameters.size();
    }

    @Override
    public IParameter[] getParameters() {
        return parameters.toArray(new IParameter[parameters.size()]);
    }

    @Override
    public String[] getParameterNames() {
        String[] names = new String[parameters.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = (parameters.getPart(i)).getName();
        }
        return names;
    }

    @Override
    public List<Datatype> getParameterDatatypes() {
        List<Datatype> parameterDatatypes = new ArrayList<Datatype>();
        try {
            for (IParameter parameter : getParameters()) {
                Datatype parameterDatatype = parameter.findDatatype(getIpsProject());
                if (parameterDatatype != null) {
                    parameterDatatypes.add(parameterDatatype);
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return parameterDatatypes;
    }

    @Override
    public int[] moveParameters(int[] indexes, boolean up) {
        return parameters.moveParts(indexes, up);
    }

    public IParameter getParameter(int i) {
        return parameters.getPart(i);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(BaseMethod.XML_ELEMENT_NAME);
    }

    @Override
    public String getSignatureString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append('(');
        IParameter[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(params[i].getDatatype());
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    protected void validateThis(MessageList result, IIpsProject ipsProject) throws CoreException {
        super.validateThis(result, ipsProject);
        if (StringUtils.isEmpty(getName())) {
            result.add(new Message(IBaseMethod.MSGCODE_NO_NAME, Messages.Method_msg_NameEmpty, Message.ERROR, this,
                    PROPERTY_NAME));
        } else {
            String complianceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, true);
            String sourceLevel = ipsProject.getJavaProject().getOption(JavaCore.COMPILER_SOURCE, true);
            IStatus status = JavaConventions.validateMethodName(getName(), sourceLevel, complianceLevel);
            if (!status.isOK()) {
                result.add(new Message(IBaseMethod.MSGCODE_INVALID_METHODNAME, Messages.Method_msg_InvalidMethodname,
                        Message.ERROR, this, PROPERTY_NAME));
            }
        }
        ValidationUtils.checkDatatypeReference(getDatatype(), true, this, PROPERTY_DATATYPE, "", result, ipsProject); //$NON-NLS-1$

        validateMultipleParameterNames(result);
        // description same locale
    }

    private void validateMultipleParameterNames(MessageList msgList) {
        List<String> parameterNames = new ArrayList<String>();
        Set<String> multipleNames = new HashSet<String>();
        for (IParameter p : getParameters()) {
            if (parameterNames.contains(p.getName())) {
                multipleNames.add(p.getName());
            }
            parameterNames.add(p.getName());
        }
        if (multipleNames.isEmpty()) {
            return;
        }
        for (String paramName : multipleNames) {
            ArrayList<ObjectProperty> objProps = new ArrayList<ObjectProperty>();
            for (int j = 0; j < parameterNames.size(); j++) {
                if (parameterNames.get(j).equals(paramName)) {
                    objProps.add(new ObjectProperty(getParameter(j), IBaseMethod.PROPERTY_PARAMETERS, j));
                }
            }
            ObjectProperty[] objectProperties = objProps.toArray(new ObjectProperty[objProps.size()]);
            String text = NLS.bind(Messages.Method_duplicateParamName, paramName);
            msgList.add(new Message(IBaseMethod.MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME, text, Message.ERROR,
                    objectProperties));
        }
    }

    @Override
    public void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        initDatatype(element);
    }

    private void initDatatype(Element element) {
        String datatypeElement = element.getAttribute(PROPERTY_DATATYPE);
        if (datatypeElement != null) {
            datatype = datatypeElement;
        }
    }

    @Override
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException {
        return ipsProject.findDatatype(getDatatype());
    }

    @Override
    public boolean isSameSignature(IBaseMethod other) {
        if (!getName().equals(other.getName())) {
            return false;
        }
        if (getNumOfParameters() != other.getNumOfParameters()) {
            return false;
        }
        IParameter[] otherParams = other.getParameters();
        for (int i = 0; i < getNumOfParameters(); i++) {
            if (!getParameter(i).getDatatype().equals(otherParams[i].getDatatype())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_DATATYPE, getDatatype());
        element.setAttribute(PROPERTY_NAME, getName());
    }

    /**
     * Sets the name with the parent's name
     */
    public void synchronizeName() {
        name = parent.getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This default implementation always returns {@link Modifier#PUBLIC}.
     */
    @Override
    public int getJavaModifier() {
        return Modifier.PUBLIC;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("BaseMethod: "); //$NON-NLS-1$
        buffer.append(getName());
        buffer.append(": "); //$NON-NLS-1$
        buffer.append(getDatatype());
        buffer.append(' ');
        buffer.append(getName());
        buffer.append('(');
        IParameter[] params = getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buffer.append(", "); //$NON-NLS-1$
            }
            buffer.append(params[i].getDatatype());
            buffer.append(' ');
            buffer.append(params[i].getName());
        }
        buffer.append(')');
        return buffer.toString();
    }

}