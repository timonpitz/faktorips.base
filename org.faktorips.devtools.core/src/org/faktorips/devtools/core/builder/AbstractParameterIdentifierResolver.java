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

package org.faktorips.devtools.core.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ListOfTypeDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * An identifier resolver that resolves identifiers against a set of <code>Parameter</code>s that
 * can be registered via the <code>add()</code> methods.
 */
public abstract class AbstractParameterIdentifierResolver implements IdentifierResolver {

    public static final char VALUE_SUFFIX_SEPARATOR_CHAR = '@';
    public static final String DEFAULT_VALUE_SUFFIX = "@default"; //$NON-NLS-1$
    private IIpsProject ipsproject;
    private IExpression formula;
    private ExprCompiler exprCompiler;

    public AbstractParameterIdentifierResolver(IExpression formula2, ExprCompiler exprCompiler) {
        ArgumentCheck.notNull(formula2, this);
        ArgumentCheck.notNull(exprCompiler, this);
        this.formula = formula2;
        this.exprCompiler = exprCompiler;
        ipsproject = formula2.getIpsProject();
    }

    private IParameter[] getParameters() {
        return formula.findFormulaSignature(ipsproject).getParameters();
    }

    private IProductCmptType getProductCmptType() {
        return formula.findProductCmptType(ipsproject);
    }

    /**
     * Provides the name of the getter method for the provided attribute.
     */
    protected abstract String getParameterAttributGetterName(IAttribute attribute, Datatype datatype);

    /**
     * Provides the name of the getter method for the provided attribute's default value.
     */
    protected abstract String getParameterAttributDefaultValueGetterName(IAttribute attribute, IPolicyCmptType type);

    /**
     * Provides the name of the getter method for a single target of the given association from the
     * given policy component type.
     */
    protected abstract String getAssociationTargetGetterName(IAssociation association, IPolicyCmptType policyCmptType);

    /**
     * Provides the name of the getter method for indexed access of one of multiple targets of the
     * given association from the given policy component type.
     */
    protected abstract String getAssociationTargetAtIndexGetterName(IAssociation association,
            IPolicyCmptType policyCmptType);

    /**
     * Provides the name of the getter method for access a list of multiple targets of the given
     * association from the given policy component type.
     */
    protected abstract String getAssociationTargetsGetterName(IAssociation association, IPolicyCmptType policyCmptType);

    /**
     * Provides the name of the Java Class generated for the given type.
     */
    protected abstract String getJavaClassName(IType type);

    private Map<String, EnumDatatype> createEnumMap() {
        EnumDatatype[] enumtypes = formula.getEnumDatatypesAllowedInFormula();
        Map<String, EnumDatatype> enumDatatypes = new HashMap<String, EnumDatatype>(enumtypes.length);
        for (EnumDatatype enumtype : enumtypes) {
            enumDatatypes.put(enumtype.getName(), enumtype);
        }
        return enumDatatypes;
    }

    @Override
    public CompilationResult compile(String identifier, ExprCompiler exprCompiler, Locale locale) {
        if (ipsproject == null) {
            throw new IllegalStateException(Messages.AbstractParameterIdentifierResolver_msgResolverMustBeSet);
        }

        String paramName;
        String attributeName;
        int pos = identifier.indexOf('.');
        if (pos == -1) {
            paramName = identifier;
            attributeName = ""; //$NON-NLS-1$
        } else {
            paramName = identifier.substring(0, pos);
            attributeName = identifier.substring(pos + 1);
        }
        IParameter[] params = getParameters();
        for (IParameter param : params) {
            if (param.getName().equals(paramName)) {
                CompilationResult result = compile(param, attributeName);
                addCurrentIdentifer(result, identifier);
                return result;
            }
        }

        /*
         * Assuming that the identifier is an attribute of the product component type where the
         * formula method is defined.
         */
        CompilationResult result = compileThis(identifier);
        if (result != null) {
            addCurrentIdentifer(result, identifier);
            return result;
        }
        result = compileEnumDatatypeValueIdentifier(paramName, attributeName);
        if (result != null) {
            /*
             * The identifier is an enumeration data type, thus it must not be added to the result
             * as know parameter identifier.
             */
            return result;
        }
        return CompilationResultImpl.newResultUndefinedIdentifier(locale, identifier);
    }

    /**
     * Adds the given identifier candidate to the compilation result.
     */
    private void addCurrentIdentifer(CompilationResult result, String identifierCandidate) {
        if (result instanceof CompilationResultImpl) {
            ((CompilationResultImpl)result).addIdentifierUsed(identifierCandidate);
        }
    }

    private CompilationResult compileThis(String identifier) {
        IProductCmptType productCmptType = null;
        try {
            productCmptType = getProductCmptType();
            List<IAttribute> attributes = formula.findMatchingProductCmptTypeAttributes();
            for (IAttribute attribute : attributes) {
                if (attribute.getName().equals(identifier)) {
                    Datatype attrDatatype = attribute.findDatatype(ipsproject);
                    if (attrDatatype == null) {
                        String text = NLS.bind(
                                Messages.AbstractParameterIdentifierResolver_msgNoDatatypeForProductCmptTypeAttribute,
                                attribute.getName(), productCmptType.getQualifiedName());
                        return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
                    }
                    /*
                     * We use "this." to access the product component type instance variable because
                     * the compiled formula code is also interpreted. When it is interpreted, it
                     * does not run inside a method of the product component generation class and so
                     * we can't access the product component generation attributes via this. So when
                     * we interpret the code, we introduce a new parameter (thiz) and replace
                     * "this." with "thiz."
                     */
                    String code = "this." + getParameterAttributGetterName(attribute, productCmptType) + "()"; //$NON-NLS-1$ //$NON-NLS-2$
                    return new CompilationResultImpl(code, attrDatatype);
                }
            }
        } catch (CoreException e) {
            String text = NLS.bind(
                    Messages.AbstractParameterIdentifierResolver_msgExceptionWhileResolvingIdentifierAtThis,
                    identifier, productCmptType == null ? "null" : productCmptType.getQualifiedName()); //$NON-NLS-1$
            IpsPlugin.log(new IpsStatus(text, e));
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
        }
        return null;
    }

    /**
     * Returns the compilation result for the a parameter and attribute name.
     */
    protected CompilationResult compile(IParameter param, String attributeName) {
        Datatype datatype;
        try {
            datatype = param.findDatatype(ipsproject);
            if (datatype == null) {
                String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgDatatypeCanNotBeResolved,
                        param.getDatatype(), param.getName());
                return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorParameterDatatypeResolving,
                    param.getDatatype(), param.getName());
            return new CompilationResultImpl(Message.newError(ExprCompiler.INTERNAL_ERROR, text));
        }
        if (datatype instanceof IType) {
            return compileTypeAttributeIdentifier(new JavaCodeFragment(param.getName()), (IType)datatype, attributeName);
        }
        if (datatype instanceof ValueDatatype) {
            return new CompilationResultImpl(param.getName(), datatype);
        }
        throw new RuntimeException("Unknown datatype class " //$NON-NLS-1$
                + datatype.getClass());
    }

    /**
     * Since the generation of a new instance statement of an {@link IEnumType} needs information
     * about the code generation the implementation is postponed to the generation implementation.
     * By default this method is an empty implementation.
     * 
     * @param fragment the {@link JavaCodeFragment} to add the new instance expression for the
     *            provided {@link IEnumType}
     * @param enumType the enumeration type
     * @param exprCompiler the expression compiler
     * @param value the value
     * 
     * @throws CoreException thrown in case of exception
     */
    protected void addNewInstanceForEnumType(JavaCodeFragment fragment,
            EnumTypeDatatypeAdapter enumType,
            ExprCompiler exprCompiler,
            String value) throws CoreException {

        // Could be implemented in subclass.
    }

    private CompilationResult compileEnumDatatypeValueIdentifier(String enumTypeName, String valueName) {
        try {
            Map<String, EnumDatatype> enumDatatypes = createEnumMap();
            EnumDatatype enumType = enumDatatypes.get(enumTypeName);
            if (enumType == null) {
                return null;
            }
            String[] valueIds = enumType.getAllValueIds(true);
            for (String enumValueName : valueIds) {
                if (ObjectUtils.equals(enumValueName, valueName)) {
                    JavaCodeFragment frag = new JavaCodeFragment();
                    frag.getImportDeclaration().add(enumType.getJavaClassName());
                    if (enumType instanceof EnumTypeDatatypeAdapter) {
                        addNewInstanceForEnumType(frag, (EnumTypeDatatypeAdapter)enumType, exprCompiler, enumValueName);
                    } else {
                        DatatypeHelper helper = ipsproject.getDatatypeHelper(enumType);
                        frag.append(helper.newInstance(enumValueName));
                    }
                    return new CompilationResultImpl(frag, enumType);
                }
            }
        } catch (Exception e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorDuringEnumDatatypeResolving,
                    enumTypeName);
            return new CompilationResultImpl(Message.newError(ExprCompiler.INTERNAL_ERROR, text));
        }
        return null;
    }

    private CompilationResult compileTypeAttributeIdentifier(JavaCodeFragment javaCodeFragment,
            IType type,
            String attributeName) {
        if (StringUtils.isEmpty(attributeName)) {
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                    Messages.AbstractParameterIdentifierResolver_msgAttributeMissing));
        }
        if (attributeName.indexOf('.') > 0) {
            return compileAssociationChain(javaCodeFragment, type, attributeName);
        }

        boolean isDefaultValueAccess = type instanceof IPolicyCmptType && attributeName.endsWith(DEFAULT_VALUE_SUFFIX);
        IAttribute attribute = null;
        try {
            attribute = findAttribute(type, attributeName, isDefaultValueAccess);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorRetrievingAttribute,
                    attributeName, type);
            return new CompilationResultImpl(Message.newError(ExprCompiler.INTERNAL_ERROR, text));
        }
        if (attribute == null && type instanceof IPolicyCmptType) {
            return compileTypeAssociationIdentifier(javaCodeFragment, type, attributeName);
        }
        if (attribute == null) {
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoAttribute, new Object[] {
                    javaCodeFragment, type.getName(), attributeName });
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
        }

        try {
            Datatype datatype = attribute.findDatatype(ipsproject);
            if (datatype == null) {
                String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoDatatypeForAttribute,
                        attribute.getDatatype(), attributeName);
                return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
            }
            String parameterAttributGetterName = isDefaultValueAccess ? getParameterAttributDefaultValueGetterName(
                    attribute, (IPolicyCmptType)type) : getParameterAttributGetterName(attribute, type);
            javaCodeFragment.append('.' + parameterAttributGetterName + "()"); //$NON-NLS-1$
            return new CompilationResultImpl(javaCodeFragment, datatype);
        } catch (Exception e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoDatatypeForAttribute,
                    attribute.getDatatype(), attributeName);
            return new CompilationResultImpl(Message.newError(ExprCompiler.INTERNAL_ERROR, text));
        }
    }

    private IAttribute findAttribute(IType type, String attributeName, boolean isDefaultValueAccess)
            throws CoreException {
        IAttribute attribute;
        String actualAttributeName = isDefaultValueAccess ? attributeName.substring(0,
                attributeName.lastIndexOf(VALUE_SUFFIX_SEPARATOR_CHAR)) : attributeName;// '@'
        attribute = type.findAttribute(actualAttributeName, ipsproject);
        return attribute;
    }

    private CompilationResult compileTypeAssociationIdentifier(JavaCodeFragment javaCodeFragment,
            IType type,
            String attributeName) throws NumberFormatException {
        try {
            String associationName = attributeName;
            Integer index = null;
            if (attributeName.indexOf('[') > 0) {
                associationName = attributeName.substring(0, attributeName.indexOf('['));
                index = Integer.valueOf(attributeName.substring(attributeName.indexOf('[') + 1,
                        attributeName.indexOf(']')));
            }
            String qualifier = null;
            if (attributeName.indexOf('(') > 0) {
                associationName = attributeName.substring(0, attributeName.indexOf('('));
                qualifier = attributeName.substring(attributeName.indexOf('(') + 1, attributeName.indexOf(')'));
            }
            IAssociation association = type.findAssociation(associationName, ipsproject);
            if (association == null) {
                String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoAttribute, new Object[] {
                        javaCodeFragment, type.getName(), attributeName });
                return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
            } else {
                if (association.is1To1() && index != null) {
                    return new CompilationResultImpl(Message.newError(ExprCompiler.NO_INDEX_FOR_1TO1_ASSOCIATION, NLS
                            .bind(Messages.AbstractParameterIdentifierResolver_noIndexFor1to1Association0,
                                    new Object[] { association, index })));
                }
                IType target = association.findTarget(ipsproject);
                if (target == null) {
                    return new CompilationResultImpl(Message.newError(
                            ExprCompiler.NO_ASSOCIATION_TARGET,
                            NLS.bind(Messages.AbstractParameterIdentifierResolver_noAssociationTarget, new Object[] {
                                    association, type.getName() })));
                }
                CompilationResult associationIdentifier = null;
                if (association.is1ToManyIgnoringQualifier() && index == null) {
                    associationIdentifier = compileTypeAssociationToManyIdentifier(javaCodeFragment, type, association,
                            target);
                } else {
                    associationIdentifier = compileTypeAssociationTo1Identifier(javaCodeFragment, index, type,
                            association, target);
                }
                if (qualifier == null || associationIdentifier.failed()) {
                    return associationIdentifier;
                } else {
                    IProductCmptType productCmptType = ((IPolicyCmptType)target).findProductCmptType(ipsproject);
                    IIpsSrcFile[] allProductCmptSrcFiles = ipsproject.findAllProductCmptSrcFiles(productCmptType, true);
                    String runtimeId = null;
                    for (IIpsSrcFile ipsSrcFile : allProductCmptSrcFiles) {
                        if (qualifier.equals(ipsSrcFile.getIpsObjectName())) {
                            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
                            if (ipsObject instanceof IProductCmpt) {
                                IProductCmpt productCmpt = (IProductCmpt)ipsObject;
                                runtimeId = productCmpt.getRuntimeId();
                                break;
                            }
                        }
                    }
                    if (runtimeId == null) {
                        String text = NLS.bind("The qualifier {0} is no product component for type {1}.", new Object[] { //$NON-NLS-1$
                                qualifier, productCmptType.getName() });
                        return new CompilationResultImpl(Message.newError(ExprCompiler.UNKNOWN_QUALIFIER, text));
                    }
                    JavaCodeFragment getQualifiedTargetCode = new JavaCodeFragment();
                    getQualifiedTargetCode.appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.class);
                    getQualifiedTargetCode.append(".getModelObjectById("); //$NON-NLS-1$
                    getQualifiedTargetCode.append(associationIdentifier.getCodeFragment());
                    getQualifiedTargetCode.append(", \""); //$NON-NLS-1$
                    getQualifiedTargetCode.append(runtimeId);
                    getQualifiedTargetCode.append("\")"); //$NON-NLS-1$
                    return new CompilationResultImpl(getQualifiedTargetCode, target);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoAttribute, new Object[] {
                    javaCodeFragment, type.getName(), attributeName });
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
        }
    }

    private CompilationResult compileTypeAssociationTo1Identifier(JavaCodeFragment javaCodeFragment,
            Integer index,
            IType type,
            IAssociation association,
            IType target) {
        if (index == null) {
            javaCodeFragment.append('.');
            javaCodeFragment.append(getAssociationTargetGetterName(association, (IPolicyCmptType)type));
            javaCodeFragment.append("()"); //$NON-NLS-1$
        } else {
            javaCodeFragment.append('.');
            javaCodeFragment.append(getAssociationTargetGetterName(association, (IPolicyCmptType)type));
            javaCodeFragment.append("(" + index.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return new CompilationResultImpl(javaCodeFragment, target);
    }

    private CompilationResult compileTypeAssociationToManyIdentifier(JavaCodeFragment javaCodeFragment,
            IType type,
            IAssociation association,
            IType target) {
        String associationTargetGetterName;
        associationTargetGetterName = getAssociationTargetsGetterName(association, (IPolicyCmptType)type);
        javaCodeFragment.append('.' + associationTargetGetterName + "()"); //$NON-NLS-1$
        return new CompilationResultImpl(javaCodeFragment, new ListOfTypeDatatype(target));
    }

    private CompilationResult compileAssociationChain(JavaCodeFragment javaCodeFragment,
            IType type,
            String attributeName) {
        String target = attributeName.substring(0, attributeName.indexOf('.'));
        CompilationResult compilationResult1 = compileTypeAttributeIdentifier(javaCodeFragment, type, target);
        String tail = attributeName.substring(attributeName.indexOf('.') + 1);
        Datatype datatype = compilationResult1.getDatatype();
        if (datatype instanceof IType) {
            return compileAssociationTo1Chain(compilationResult1, tail, datatype);
        } else if (datatype instanceof ListOfTypeDatatype) {
            return compileAssociationToManyChain(compilationResult1.getCodeFragment(), (ListOfTypeDatatype)datatype,
                    tail);
        } else {
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoAttribute, new Object[] {
                    javaCodeFragment, type.getName(), attributeName });
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
        }
    }

    private CompilationResult compileAssociationTo1Chain(CompilationResult compilationResult1,
            String tail,
            Datatype datatype) {
        CompilationResult compilationResult2 = compileTypeAttributeIdentifier(compilationResult1.getCodeFragment(),
                (IType)datatype, tail);
        MessageList messages = compilationResult1.getMessages();
        messages.add(compilationResult2.getMessages());
        Set<String> identifiers = new HashSet<String>();
        Collections.addAll(identifiers, compilationResult1.getResolvedIdentifiers());
        Collections.addAll(identifiers, compilationResult2.getResolvedIdentifiers());
        JavaCodeFragment codeFragment = compilationResult2.getCodeFragment();
        codeFragment.getImportDeclaration().add(compilationResult1.getCodeFragment().getImportDeclaration());
        return new CompilationResultImpl(codeFragment, compilationResult2.getDatatype(), messages, identifiers);
    }

    private CompilationResult compileAssociationToManyChain(JavaCodeFragment javaCodeFragment,
            ListOfTypeDatatype datatype,
            String code) {
        if (StringUtils.isEmpty(code)) {
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER,
                    Messages.AbstractParameterIdentifierResolver_msgAttributeMissing));
        }
        String associationName = code;
        String tail = StringUtils.EMPTY;
        if (code.indexOf('.') > 0) {
            associationName = code.substring(0, code.indexOf('.'));
            tail = code.substring(code.indexOf('.') + 1);
        }
        String actualAssociationName = associationName;
        Integer index = null;
        if (associationName.indexOf('[') > 0) {
            actualAssociationName = associationName.substring(0, associationName.indexOf('['));
            index = Integer.valueOf(associationName.substring(associationName.indexOf('[') + 1,
                    associationName.indexOf(']')));
        }
        IType type = (IType)datatype.getBasicDatatype();
        try {
            IAssociation association = type.findAssociation(actualAssociationName, ipsproject);
            if (association != null) {
                IType target = association.findTarget(ipsproject);
                if (target == null) {
                    return new CompilationResultImpl(Message.newError(
                            ExprCompiler.NO_ASSOCIATION_TARGET,
                            NLS.bind(Messages.AbstractParameterIdentifierResolver_noAssociationTarget, new Object[] {
                                    association, type.getName() })));
                }
                JavaCodeFragment getTargetCode = compileAssociationAccess(javaCodeFragment, association);
                if (index == null) {
                    if (tail.isEmpty()) {
                        return new CompilationResultImpl(getTargetCode, new ListOfTypeDatatype(target));
                    } else {
                        return compileAssociationToManyChain(getTargetCode, new ListOfTypeDatatype(target), tail);
                    }
                } else {
                    getTargetCode.append(".get(" + index.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (tail.isEmpty()) {
                        return new CompilationResultImpl(getTargetCode, target);
                    } else {
                        return compileTypeAttributeIdentifier(getTargetCode, target, tail);
                    }
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
            String text = NLS.bind(Messages.AbstractParameterIdentifierResolver_msgErrorNoAttribute, new Object[] {
                    javaCodeFragment, type.getName(), code });
            return new CompilationResultImpl(Message.newError(ExprCompiler.UNDEFINED_IDENTIFIER, text));
        }
        return null;
    }

    private JavaCodeFragment compileAssociationAccess(JavaCodeFragment javaCodeFragment, IAssociation association)
            throws CoreException {
        JavaCodeFragment getTargetCode = new JavaCodeFragment();
        getTargetCode.appendClassName(org.faktorips.runtime.formula.FormulaEvaluatorUtil.class);
        getTargetCode.append(".getTargets("); //$NON-NLS-1$
        getTargetCode.append(javaCodeFragment);
        getTargetCode.append(", \"" + association.getName() + "\", "); //$NON-NLS-1$ //$NON-NLS-2$
        IType target = association.findTarget(association.getIpsProject());
        getTargetCode.appendClassName(getJavaClassName(target));
        getTargetCode.append(".class, this.getRepository())"); //$NON-NLS-1$
        return getTargetCode;
    }

}
