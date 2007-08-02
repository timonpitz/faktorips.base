/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.BuilderHelper;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.table.TableImplBuilder;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.internal.EnumValues;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Range;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;
import org.faktorips.valueset.EnumValueSet;
import org.faktorips.valueset.IntegerRange;
import org.w3c.dom.Element;

/**
 * Builder that generates Java sourcefiles (compilation units) containing the sourcecode for the
 * published interface of a product component generation.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenImplClassBuilder extends AbstractProductCmptTypeBuilder{
    
    // property key for the constructor's Javadoc.
    private final static String GET_TABLE_USAGE_METHOD_JAVADOC = "GET_TABLE_USAGE_METHOD_JAVADOC";
    
    public static final String XML_ATTRIBUTE_TARGET_RUNTIME_ID = "targetRuntimeId";
    
    private ProductCmptGenInterfaceBuilder interfaceBuilder;
    private ProductCmptImplClassBuilder productCmptTypeImplCuBuilder;
    private ProductCmptInterfaceBuilder productCmptTypeInterfaceBuilder;
    private PolicyCmptImplClassBuilder policyCmptTypeImplBuilder;
    private TableImplBuilder tableImplBuilder;
    
    public ProductCmptGenImplClassBuilder(IIpsArtefactBuilderSet builderSet, String kindId) {
        super(builderSet, kindId, new LocalizedStringsSet(ProductCmptGenImplClassBuilder.class));
        setMergeEnabled(true);
    }
    
    public void setInterfaceBuilder(ProductCmptGenInterfaceBuilder builder) {
        ArgumentCheck.notNull(builder);
        this.interfaceBuilder = builder;
    }
    
    public void setProductCmptTypeImplBuilder(ProductCmptImplClassBuilder builder) {
        ArgumentCheck.notNull(builder);
        productCmptTypeImplCuBuilder = builder;
    }
    
    public void setPolicyCmptTypeImplBuilder(PolicyCmptImplClassBuilder builder) {
        this.policyCmptTypeImplBuilder = builder;
    }
    
    public void setProductCmptTypeInterfaceBuilder(ProductCmptInterfaceBuilder builder) {
        this.productCmptTypeInterfaceBuilder = builder;
    }

    public void setTableImplBuilder(TableImplBuilder tableImplBuilder) {
        this.tableImplBuilder = tableImplBuilder;
    }

    /**
     * If a policy component type contains an derived or computed attribute, the product component
     * generation class must be abstract, as the computation formulas are defined per generation. 
     * 
     * {@inheritDoc}
     */
    protected int getClassModifier() throws CoreException {
        int modifier = super.getClassModifier();
        if ((modifier & Modifier.ABSTRACT) > 0) {
            return modifier;
        }
        GetClassModifierFunction fct = new GetClassModifierFunction(modifier);
        fct.start(getProductCmptType().findPolicyCmptyType());
        return fct.getModifier();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        String generationAbb = getAbbreviationForGenerationConcept(ipsSrcFile);
        return getJavaNamingConvention().getImplementationClassName(getProductCmptType(ipsSrcFile).getName() + generationAbb);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean generatesInterface() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected String getSuperclass() throws CoreException {
        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype != null) {
            String pack = getPackage(supertype.getIpsSrcFile());
            return StringUtil.qualifiedName(pack, getUnqualifiedClassName(supertype.getIpsSrcFile()));
        }
        return ProductComponentGeneration.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    protected String[] getExtendedInterfaces() throws CoreException {
        // The implementation implements the published interface.
        return new String[] { interfaceBuilder.getQualifiedClassName(getIpsSrcFile()) };
    }

    /**
     * {@inheritDoc}
     */
    protected void generateTypeJavadoc(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CLASS", interfaceBuilder.getUnqualifiedClassName(getIpsSrcFile()), getIpsObject(), builder);
    }

    /**
     * {@inheritDoc}
     */
    protected void generateConstructors(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("CONSTRUCTOR", getUnqualifiedClassName(), getIpsObject(), builder);
        builder.append("public ");
        builder.append(getUnqualifiedClassName());
        builder.append('(');
        builder.appendClassName(productCmptTypeImplCuBuilder.getQualifiedClassName(getIpsSrcFile()));
        builder.append(" productCmpt)");
        builder.openBracket();
        builder.appendln("super(productCmpt);");
        builder.closeBracket();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateOtherCode(JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        
        generateMethodDoInitPropertiesFromXml(methodsBuilder);
        generateMethodDoInitReferencesFromXml(methodsBuilder);
        generateMethodDoInitTableUsagesFromXml(methodsBuilder);
    }
    
    private void generateMethodDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        builder.methodBegin(Modifier.PROTECTED, Void.TYPE, "doInitPropertiesFromXml", 
                new String[]{"configMap"}, new Class[]{Map.class});
        
        builder.appendln("super.doInitPropertiesFromXml(configMap);");
        IAttribute[] attributes = getProductCmptType().getAttributes();
        boolean attributeFound = false;
        for (int i = 0; i < attributes.length; i++) {
            IAttribute a = attributes[i];
            if (a.validate().containsErrorMsg()) {
                continue;
            }
            if (!a.isProductRelevant() || a.isDerived()) {
                continue;
            }
            if (attributeFound == false) {
                builder.appendClassName(Element.class);
                builder.appendln(" configElement = null;");
                builder.appendClassName(String.class);
                builder.appendln(" value = null;");
                attributeFound = true;
            }
            ValueDatatype datatype = a.findDatatype();
            DatatypeHelper helper = getProductCmptType().getIpsProject().getDatatypeHelper(datatype);
            String memberVarName;
            if (a.isChangeable()) {
                memberVarName = getFieldNameDefaulValue(a);
            } else {
                memberVarName = getFieldNameValue(a);
            }
            builder.append("configElement = (");
            builder.appendClassName(Element.class);
            builder.append(")configMap.get(\"");
            builder.append(a.getName());
            builder.appendln("\");");
            builder.append("if (configElement != null) ");
            builder.openBracket();
            builder.append("value = ");
            builder.appendClassName(ValueToXmlHelper.class);
            builder.append(".getValueFromElement(configElement, \"Value\");");
            builder.append(memberVarName);
            builder.append(" = ");
            builder.append(helper.newInstanceFromExpression("value"));
            builder.appendln(";");
            
            if(AttributeType.CHANGEABLE.equals(a.getAttributeType())){
                ValueSetType valueSetType = a.getValueSet().getValueSetType();
                JavaCodeFragment frag = new JavaCodeFragment();
                helper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), helper);
                if(ValueSetType.RANGE.equals(valueSetType)){
                    frag.appendClassName(Range.class);
                    frag.append(" range = ");
                    frag.appendClassName(ValueToXmlHelper.class);
                    frag.appendln(".getRangeFromElement(configElement, \"ValueSet\");");
                    frag.append(getFieldNameRangeFor(a));
                    frag.append(" = ");
                    JavaCodeFragment newRangeInstanceFrag = helper.newRangeInstance(
                            new JavaCodeFragment("range.getLower()"), new JavaCodeFragment("range.getUpper()"),
                            new JavaCodeFragment("range.getStep()"), new JavaCodeFragment("range.containsNull()"));
                    if(newRangeInstanceFrag == null){
                        throw new CoreException(new IpsStatus("The " + helper + " for the datatype " +  datatype.getName() + " doesn't support ranges."));
                    }
                    frag.append(newRangeInstanceFrag);
                    frag.appendln(";");
                }
                else if(ValueSetType.ENUM.equals(valueSetType)){
                    frag.appendClassName(EnumValues.class);
                    frag.append(" values = ");
                    frag.appendClassName(ValueToXmlHelper.class);
                    frag.appendln(".getEnumValueSetFromElement(configElement, \"ValueSet\");");
                    frag.appendClassName(ArrayList.class);
                    frag.append(" enumValues = new ");
                    frag.appendClassName(ArrayList.class);
                    frag.append("();");
                    frag.append("for (int i = 0; i < values.getNumberOfValues(); i++)");
                    frag.appendOpenBracket();
                    frag.append("enumValues.add(");
                    frag.append(helper.newInstanceFromExpression("values.getValue(i)"));
                    frag.appendln(");");
                    frag.appendCloseBracket();
                    frag.append(getFieldNameAllowedValuesFor(a));
                    frag.append(" = ");
                    frag.append(helper.newEnumValueSetInstance(new JavaCodeFragment("enumValues"), 
                            new JavaCodeFragment("values.containsNull()")));
                    frag.appendln(";");
                }
                builder.append(frag);
            }
            
            builder.closeBracket();
        }
        builder.methodEnd();
    }
    
    private void generateMethodDoInitReferencesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String[] argNames = new String[]{"relationMap"};
        String[] argTypes = new String[]{Map.class.getName()};
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitReferencesFromXml", argNames, argTypes);
        builder.appendln("super.doInitReferencesFromXml(relationMap);");
        
        // before the first relation we define a temp variable as follows:
        // Element relationElements = null;

        // for each 1-1 relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        //     vertragsteilePk = ((Element)relationElement.get(0)).getAttribute("targetRuntimeId");
        // }
        // 
        // for each 1-many relation in the policy component type we generate:
        // relationElements = (ArrayList) relationMap.get("Product");
        // if(relationElement != null) {
        //     vertragsteilPks[] = new VertragsteilPk[relationElements.length()];
        //     for (int i=0; i<vertragsteilsPks.length; i++) {
        //         vertragsteilPks[i] = ((Element)relationElement.get(i)).getAttribute("targetRuntimeId");
        //         }
        //     }
        // }
        IProductCmptTypeRelation[] relations = getProductCmptType().getRelations();
        boolean relationFound = false;
        for (int i = 0; i < relations.length; i++) {
            IProductCmptTypeRelation r = relations[i];
            if (!r.isAbstract()) {
                if (relationFound == false) {
                    builder.appendln();
                    builder.appendClassName(List.class);
                    builder.append(" ");
                    relationFound = true;
                }
                builder.append("relationElements = (");
                builder.appendClassName(List.class);
                builder.append(") relationMap.get(");
                builder.appendQuoted(r.getName());
                builder.appendln(");");
                builder.append("if (relationElements != null) {");
                if (r.is1ToMany()) {
                    String fieldName = getFieldNameToManyRelation(r);
                    String cardinalityFieldName = getFieldNameCardinalityForRelation(r);
                    builder.append(fieldName);
                    builder.appendln(" = new ");
                    builder.appendClassName(String.class);
                    builder.appendln("[relationElements.size()];");
                    builder.append(cardinalityFieldName);
                    builder.append(" = new ");
                    builder.appendClassName(HashMap.class);
                    builder.appendln("(relationElements.size());");
                    builder.appendln("for (int i=0; i<relationElements.size(); i++) {");
                    builder.appendClassName(Element.class);
                    builder.append(" element = (");
                    builder.appendClassName(Element.class);
                    builder.appendln(")relationElements.get(i);");
                    builder.append(fieldName);
                    builder.append("[i] = ");
                    builder.appendln("element.getAttribute(\"" + XML_ATTRIBUTE_TARGET_RUNTIME_ID + "\");");
                    builder.append("addToCardinalityMap(");
                    builder.append(cardinalityFieldName);
                    builder.append(", ");
                    builder.append(fieldName);
                    builder.append("[i], ");
                    builder.appendln("element);");
                    builder.appendln("}");
                } else {
                    String fieldName = getFieldNameTo1Relation(r);
                    String cardFieldName = getFieldNameCardinalityForRelation(r);
                    builder.appendClassName(Element.class);
                    builder.append(" element = (");
                    builder.appendClassName(Element.class);
                    builder.appendln(")relationElements.get(0);");
                    builder.append(fieldName);
                    builder.append(" = ");
                    builder.appendln("element.getAttribute(\"" + XML_ATTRIBUTE_TARGET_RUNTIME_ID + "\");");
                    builder.append(cardFieldName);
                    builder.append(" = new ");
                    builder.appendClassName(HashMap.class);
                    builder.appendln("(1);");
                    builder.append("addToCardinalityMap(");
                    builder.append(cardFieldName);
                    builder.append(", ");
                    builder.append(fieldName);
                    builder.appendln(", element);");
                }
                builder.appendln("}");
            }
        }
        builder.methodEnd();
    }

    private void generateMethodDoInitTableUsagesFromXml(JavaCodeFragmentBuilder builder) {
        ITableStructureUsage[] tsus = getProductCmptType().getTableStructureUsages();
        if (tsus.length == 0){
            return;
        }
        String javaDoc = null;
        builder.javaDoc(javaDoc, ANNOTATION_GENERATED);
        String[] argNames = new String[]{"tableUsageMap"};
        String[] argTypes = new String[]{Map.class.getName()};
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitTableUsagesFromXml", argNames, argTypes);
        builder.appendln("super.doInitTableUsagesFromXml(tableUsageMap);");
        builder.appendClassName(Element.class);
        builder.appendln(" element = null;");
        for (int i = 0; i < tsus.length; i++) {
            builder.append(" element = (");
            builder.appendClassName(Element.class);
            builder.append(") tableUsageMap.get(\"");
            builder.append(tsus[i].getRoleName());
            builder.appendln("\");");
            builder.appendln("if (element != null){");
            builder.append(getTableStructureUsageRoleName(tsus[i]));
            builder.appendln(" = ");
            builder.appendClassName(ValueToXmlHelper.class);
            builder.append(".getValueFromElement(element, \"TableContentName\");");
            builder.appendln("};");
        }
        builder.appendln("};");
    }
    
    /*
     * Generates the method to return the table content which is related to the specific role.<br>
     * Example:
     * <code>
     *   public FtTable getRatePlan() {
     *      if (ratePlanName == null) {
     *          return null;
     *      }
     *      return (FtTable)getRepository().getTable(ratePlanName);
     *  }
     * <code>
     */
    private void generateMethodGetTableStructure(ITableStructureUsage tsu, JavaCodeFragmentBuilder codeBuilder)
            throws CoreException {
        // generate the method to return the corresponding table content
        String methodName = getMethodNameGetTableUsage(tsu);
        String[] tss = tsu.getTableStructures();
        // get the class name of the instance which will be returned,
        // if the usage contains only one table structure then the returned class will be the
        // generated class of this table structure, otherwise the return class will be the ITable interface class
        String tableStructureClassName = "";
        if (tss.length == 1) {
            tableStructureClassName = tss[0];
            ITableStructure ts = (ITableStructure) getProductCmptType().getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, tableStructureClassName);
            if (ts == null){
                // abort because table structure not found
                return;
            }
            tableStructureClassName = tableImplBuilder.getQualifiedClassName(ts.getIpsSrcFile());
        } else if (tss.length > 1) {
            tableStructureClassName = ITable.class.getName();
        } else {
            // if no table structure is related, do nothing, this is a validation error
            return;
        }

        String javaDoc = getLocalizedText(getIpsSrcFile(), GET_TABLE_USAGE_METHOD_JAVADOC, tsu.getRoleName());
        String roleName = getTableStructureUsageRoleName(tsu);
        JavaCodeFragment body = new JavaCodeFragment();
        body.append("if (");
        body.append(roleName);
        body.appendln(" == null){");
        body.appendln("return null;");
        body.appendln("}");
        body.append("return ");
        body.append("(");
        body.appendClassName(tableStructureClassName);
        body.append(")");
        body.append(MethodNames.GET_REPOSITORY);
        body.append("().getTable(");
        body.append(roleName);
        body.appendln(");");
        codeBuilder.method(Modifier.PUBLIC, tableStructureClassName, methodName, new String[0], new String[0], body,
                javaDoc, ANNOTATION_GENERATED);
    }

    private String getMethodNameGetTableUsage(ITableStructureUsage tsu) {
        return "get" + StringUtils.capitalise(tsu.getRoleName());
    }
    
    protected void generateCodeForChangeableAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateFieldDefaultValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetDefaultValue(a, datatypeHelper, methodsBuilder);

        //if the datatype is a primitive datatype the datatypehelper will be switched to the helper of the 
        //wrapper type
        datatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(getIpsSrcFile().getIpsProject(), datatypeHelper);
        if(ValueSetType.RANGE.equals(a.getValueSet().getValueSetType())){
            generateFieldRangeFor(a , datatypeHelper, memberVarsBuilder);
            generateMethodGetRangeFor(a, datatypeHelper, methodsBuilder);
        }
        else if(ValueSetType.ENUM.equals(a.getValueSet().getValueSetType()) ||
                datatypeHelper.getDatatype() instanceof EnumDatatype){
            generateFieldAllowedValuesFor(a, memberVarsBuilder);
            generateMethodGetAllowedValuesFor(a, datatypeHelper.getDatatype(), methodsBuilder);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer minAge;
     * </pre>
     */
    private void generateFieldDefaultValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_DEFAULTVALUE", a.getName(), a, memberVarsBuilder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameDefaulValue(a), defaultValueExpression);
    }    
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getDefaultMinAge() {
     *     return minAge;
     * </pre>
     */
    private void generateMethodGetDefaultValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetDefaultValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameDefaulValue(a));
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameDefaulValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameDefaultValue(a));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForConstantAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder, JavaCodeFragmentBuilder constantBuilder) throws CoreException {
        generateFieldValue(a, datatypeHelper, memberVarsBuilder);
        generateMethodGetValue(a, datatypeHelper, methodsBuilder);
        generateMethodSetValue(a, datatypeHelper, methodsBuilder);
    }
    
    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * private Integer taxRate;
     * </pre>
     */
    private void generateFieldValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_VALUE", StringUtils.capitalise(a.getName()), a, builder);
        JavaCodeFragment defaultValueExpression = datatypeHelper.newInstance(a.getDefaultValue());
        builder.varDeclaration(Modifier.PRIVATE, datatypeHelper.getJavaClassName(),
                getFieldNameValue(a), defaultValueExpression);
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Integer getInterestRate() {
     *     return interestRate;
     * </pre>
     */
    private void generateMethodGetValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetValue(a, datatypeHelper, methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(getFieldNameValue(a));
        methodsBuilder.append(';');
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setInterestRate(Decimal newValue) {
     *     if (getRepository()!=null && !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     this.interestRate = newValue;
     * }
     * </pre>
     */
    private void generateMethodSetValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_SET_VALUE", a.getName(), a, methodsBuilder);
        String methodName = getJavaNamingConvention().getSetterMethodName(interfaceBuilder.getPropertyNameValue(a), datatypeHelper.getDatatype());
        String[] paramNames = new String[]{"newValue"};
        String[] paramTypes = new String[]{datatypeHelper.getJavaClassName()};
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, paramNames, paramTypes);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append("this." + getFieldNameValue(a));
        methodsBuilder.appendln(" = newValue;");
        methodsBuilder.closeBracket();
    }
    
    private JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()." + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
    }
    
    private String getFieldNameValue(IAttribute a) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameValue(a));
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForComputedAndDerivedAttribute(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        generateMethodComputeValue(a, datatypeHelper, methodsBuilder);
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public abstract Money computePremium(Policy policy, Integer age);
     * </pre>
     */
    public void generateMethodComputeValue(IAttribute a, DatatypeHelper datatypeHelper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_COMPUTE_VALUE", StringUtils.capitalise(a.getName()), a, methodsBuilder);
        generateSignatureComputeValue(a, datatypeHelper,Modifier.ABSTRACT | Modifier.PUBLIC, false, methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * public abstract Money computePremium(Policy policy, Integer age)
     * </pre>
     */
    public void generateSignatureComputeValue(IAttribute a, DatatypeHelper datatypeHelper, int modifier, boolean withFinalParameters, JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        Parameter[] parameters = a.getFormulaParameters();
        String methodName = getMethodNameComputeValue(a);
        methodsBuilder.signature(modifier, datatypeHelper.getJavaClassName(),
                methodName, BuilderHelper.extractParameterNames(parameters),
                StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters, a.getIpsProject(), policyCmptTypeImplBuilder),
                withFinalParameters);
        methodsBuilder.append(" throws ");
        methodsBuilder.appendClassName(FormulaExecutionException.class);
    }
    
    public String getMethodNameComputeValue(IAttribute a) {
        return getLocalizedText(a, "METHOD_COMPUTE_VALUE_NAME", StringUtils.capitalise(a.getName()));
    }

    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForTableUsage(ITableStructureUsage tsu,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        // generate the code for the fields role name which will be initialized with the related
        // table content qualified name
        appendLocalizedJavaDoc("FIELD_TABLE_USAGE", tsu.getRoleName(), tsu, fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment("null");
        fieldsBuilder.varDeclaration(Modifier.PROTECTED, String.class, getTableStructureUsageRoleName(tsu), expression);
        // generate the code for the table getter methods
        generateMethodGetTableStructure(tsu, methodsBuilder);
    }

    public String getTableStructureUsageRoleName(ITableStructureUsage tsu){
        return tsu.getRoleName() + "Name";
    }
    
    /**
     * {@inheritDoc}
     */
    protected void generateCodeForNoneContainerRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        if (relation.is1ToMany()) {
            generateFieldToManyRelation(relation, memberVarsBuilder);
            generateMethodGetManyRelatedCmpts(relation, methodsBuilder);
            generateMethodGetRelatedCmptAtIndex(relation, methodsBuilder);
            generateMethodAddRelatedCmpt(relation, methodsBuilder);
        } else {
            generateFieldTo1Relation(relation, memberVarsBuilder);
            generateMethodGet1RelatedCmpt(relation, methodsBuilder);
            generateMethodSet1RelatedCmpt(relation, methodsBuilder);
//            generateMethodGetCardinalityFor1To1Relation(relation, methodsBuilder);
//            generateFieldCardinalityFor1To1Relation(relation, memberVarsBuilder);
        }
        generateMethodGetCardinalityFor1ToManyRelation(relation, methodsBuilder);
        generateFieldCardinalityForRelation(relation, memberVarsBuilder);
        generateMethodGetNumOfRelatedProductCmpts(relation, methodsBuilder);  
    }
    
    private String getFieldNameToManyRelation(IProductCmptTypeRelation relation) throws CoreException {
        return getJavaNamingConvention().getMultiValueMemberVarName(interfaceBuilder.getPropertyNameToManyRelation(relation));
    }
    
    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * private CoverageType[] optionalCoverageTypes;
     * </pre>
     */
    private void generateFieldToManyRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalise(relation.getTargetRolePlural());
        appendLocalizedJavaDoc("FIELD_TOMANY_RELATION", role, relation, memberVarsBuilder);
        String type = String.class.getName() + "[]";
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, type, getFieldNameToManyRelation(relation), new JavaCodeFragment("new String[0]"));
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[coverageTypes.length];
     *     for (int i = 0; i < result.length; i++) {
     *         result[i] = (ICoverageType) getRepository().getProductComponent(coverageTypes[i]);
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetManyRelatedCmpts(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetManyRelatedCmpts(relation, methodsBuilder);

        String fieldName = getFieldNameToManyRelation(relation);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln(".length];");

        methodsBuilder.appendln("for (int i=0; i<result.length; i++) {");
        methodsBuilder.appendln("result[i] = (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.appendln("[i]);");
        methodsBuilder.appendln("}");
        methodsBuilder.appendln("return result;");
    
        methodsBuilder.closeBracket();
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverageType(ICoverageType[] target) {
     *     if (getRepository()!=null && !getRepository().isModifiable()) {
     *         throw new IllegalRepositoryModificationException();
     *     }
     *     String[] tmp = new String[coverageTypes.length+1];
     *     System.arraycopy(coverageTypes, 0, tmp, 0, coverageTypes.length);
     *     tmp[tmp.length-1] = target.getId();
     *     coverageTypes = tmp;
     * }
     * </pre>
     */
    private void generateMethodAddRelatedCmpt(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        IProductCmptType target = relation.findTarget();
        appendLocalizedJavaDoc("METHOD_ADD_RELATED_CMPT", relation.getTargetRoleSingular(), relation, methodsBuilder);
        String methodName = "add" + StringUtils.capitalise(relation.getTargetRoleSingular());
        String[] argNames = new String[]{"target"};
        String[] argTypes = new String[]{productCmptTypeInterfaceBuilder.getQualifiedClassName(target)};
        methodsBuilder.signature(getJavaNamingConvention().getModifierForPublicInterfaceMethod(), 
                "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameToManyRelation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.appendln("String[] tmp = new String[this." + fieldName + ".length+1];");
        methodsBuilder.appendln("System.arraycopy(this." + fieldName + ", 0, tmp, 0, this." + fieldName + ".length);");
        methodsBuilder.appendln("tmp[tmp.length-1] = " + argNames[0] + "."  + MethodNames.GET_PRODUCT_COMPONENT_ID + "();");
        methodsBuilder.appendln("this." + fieldName + " = tmp;");
        methodsBuilder.closeBracket();
    }
        
    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType(int index) {
     *     return (ICoverageType) getRepository().getProductComponent(coverageTypes[index]);
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptAtIndex(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRelatedCmptsAtIndex(relation, methodsBuilder);
        String fieldName = getFieldNameToManyRelation(relation);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append("[index]);");
        methodsBuilder.closeBracket();
    }
    
    private String getFieldNameTo1Relation(IProductCmptTypeRelation relation) throws CoreException {
        return getJavaNamingConvention().getMemberVarName(interfaceBuilder.getPropertyNameTo1Relation(relation));
    }

    /**
     * Code sample for 
     * <pre>
     * [javadoc]
     * private CoverageType mainCoverage;
     * </pre>
     */
    private void generateFieldTo1Relation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder memberVarsBuilder) throws CoreException {
        String role = StringUtils.capitalise(relation.getTargetRoleSingular());
        appendLocalizedJavaDoc("FIELD_TO1_RELATION", role, relation, memberVarsBuilder);
        memberVarsBuilder.varDeclaration(Modifier.PRIVATE, String.class, getFieldNameTo1Relation(relation), new JavaCodeFragment("null"));
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getMainCoverageType() {
     *     return (CoveragePk) getRepository().getProductComponent(mainCoverageType);
     * }
     * </pre>
     */
    private void generateMethodGet1RelatedCmpt(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGet1RelatedCmpt(relation, methodsBuilder);
        String fieldName = getFieldNameTo1Relation(relation);
        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.append("return (");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append(")getRepository()." + MethodNames.GET_EXISTING_PRODUCT_COMPONENT + "(");
        methodsBuilder.append(fieldName);
        methodsBuilder.append(");");
        methodsBuilder.closeBracket();
    }

    /**
     * Code sample:
     * <pre>
     * [javadoc]
     * public void setMainCoverageType(ICoverageType target) {
     *     mainCoverageType = target==null ? null : target.getId();
     * }
     * </pre>
     */
    private void generateMethodSet1RelatedCmpt(
            IProductCmptTypeRelation relation, 
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_SET_1_RELATED_CMPT", relation.getTargetRoleSingular(), relation, methodsBuilder);

        String propName = interfaceBuilder.getPropertyNameTo1Relation(relation);
        String methodName = getJavaNamingConvention().getSetterMethodName(propName, Datatype.INTEGER);
        String[] argNames = new String[]{"target"};
        String[] argTypes = new String[]{productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget())};
        methodsBuilder.signature(Modifier.PUBLIC, "void", methodName, argNames, argTypes);
        String fieldName = getFieldNameTo1Relation(relation);
        methodsBuilder.openBracket();
        methodsBuilder.append(generateFragmentCheckIfRepositoryIsModifiable());
        methodsBuilder.append(fieldName + " = (" + argNames[0] + "==null ? null : " + argNames[0] + "." + MethodNames.GET_PRODUCT_COMPONENT_ID + "() );");
        methodsBuilder.closeBracket();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationDefinition(IProductCmptTypeRelation containerRelation, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateCodeForContainerRelationImplementation(IProductCmptTypeRelation containerRelation, List implRelations, JavaCodeFragmentBuilder memberVarsBuilder, JavaCodeFragmentBuilder methodsBuilder) throws Exception {
        generateMethodGetRelatedCmptsInContainer(containerRelation, implRelations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmpts(containerRelation, implRelations, methodsBuilder);
        generateMethodGetNumOfRelatedProductCmptsInternal(containerRelation, implRelations, methodsBuilder);
    }
    
    /**
     * Code sample where a 1-1 and a 1-many relation implement a container relation.
     * <pre>
     * [Javadoc]
     * public ICoverageType[] getCoverageTypes() {
     *     ICoverageType[] result = new ICoverageType[getNumOfCoverageTypes()];
     *     int index = 0;
     *     if (collisionCoverageType!=null) {
     *         result[index++] = getCollisionCoverageType();
     *     }
     *     ITplCoverageType[] tplCoverageTypesObjects = getTplcCoverageTypes();
     *     for (int i=0; i<tplCoverageTypesObjects.length; i++) {
     *         result[index++] = tplCoverageTypes[i];
     *     }
     *     return result;
     * }
     * </pre>
     */
    private void generateMethodGetRelatedCmptsInContainer(
            IProductCmptTypeRelation relation,
            List implRelations,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureContainerRelation(relation, methodsBuilder);

        String targetClass = productCmptTypeInterfaceBuilder.getQualifiedClassName(relation.findTarget());
        methodsBuilder.openBracket();
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[] result = new ");
        methodsBuilder.appendClassName(targetClass);
        methodsBuilder.append("[");
        methodsBuilder.append(getMethodNameGetNumOfRelatedCmptsInternal(relation));
        methodsBuilder.appendln("()];");

        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype!=null && !supertype.isAbstract()) {
            // ICoverage[] superResult = super.getCoverages();
            // System.arraycopy(superResult, 0, result, 0, superResult.length);
            // int counter = superResult.length;
            methodsBuilder.appendClassName(targetClass);
            methodsBuilder.append("[] superResult = super.");       
            methodsBuilder.appendln(interfaceBuilder.getMethodNameGetManyRelatedCmpts(relation) + "();");
            methodsBuilder.appendln("System.arraycopy(superResult, 0, result, 0, superResult.length);");
            methodsBuilder.appendln("int index = superResult.length;");
        } else {
            methodsBuilder.append("int index = 0;");
        }
        for (Iterator it = implRelations.iterator(); it.hasNext();) {
            IProductCmptTypeRelation implRelation = (IProductCmptTypeRelation)it.next();
            if (implRelation.is1ToMany()) {
                String objectArrayVar = getFieldNameToManyRelation(implRelation) + "Objects";
                String getterMethod = interfaceBuilder.getMethodNameGetManyRelatedCmpts(implRelation) + "()";
                methodsBuilder.appendClassName(productCmptTypeInterfaceBuilder.getQualifiedClassName(implRelation.findTarget()));
                methodsBuilder.append("[] " + objectArrayVar + " = " + getterMethod + ";");
                methodsBuilder.appendln("for (int i=0; i<" + objectArrayVar + ".length; i++) {");
                methodsBuilder.appendln("result[index++] = " + objectArrayVar + "[i];");
                methodsBuilder.appendln("}");
            } else {
                String accessCode;
                if (implRelation.isAbstractContainer()) {
                    // if the implementation relation is itself a container relation, use the access method
                    accessCode = interfaceBuilder.getMethodNameGet1RelatedCmpt(implRelation) + "()";
                } else {
                    // otherwise use the field.
                    accessCode = getFieldNameTo1Relation(implRelation);
                }
                methodsBuilder.appendln("if (" + accessCode + "!=null) {");
                methodsBuilder.appendln("result[index++] = " + interfaceBuilder.getMethodNameGet1RelatedCmpt(implRelation) + "();");
                methodsBuilder.appendln("}");
            }
        }
        methodsBuilder.appendln("return result;");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Generates the getNumOfXXX() method for none container relations. 
     * <p>
     * Code sample for 1-1 relations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageType==null ? 0 : 1;
     * }
     * </pre>
     * <p>
     * Code sample for 1-many relations:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return coverageTypes.length;
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmpts(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder builder) throws CoreException {
        if (relation.isAbstractContainer()) {
            throw new IllegalArgumentException("Relation needn't be a container relation.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRelatedCmpts(relation, builder);
        builder.openBracket();
        builder.append("return ");
        if (relation.is1ToMany()) {
            builder.append(getFieldNameToManyRelation(relation));
            builder.appendln(".length;");
        } else {
            builder.append(getFieldNameTo1Relation(relation));
            builder.appendln(" ==null ? 0 : 1;");
        }
        builder.closeBracket();
    }

    /**
     * Generates the getNumOfXXX() method for a container relation. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypes() {
     *     return getNumOfCoverageTypesInternal();
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmpts(IProductCmptTypeRelation containerRelation, List implRelations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!containerRelation.isAbstractContainer()) {
            throw new IllegalArgumentException("Relation must be a container relation.");
        }
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetNumOfRelatedCmpts(containerRelation, builder);
        builder.openBracket();
        String internalMethodName = getMethodNameGetNumOfRelatedCmptsInternal(containerRelation);
        builder.appendln("return " + internalMethodName + "();");
        builder.closeBracket();
    }
    
    /**
     * Generates the getNumOfXXXInternal() method for a container relation. 
     * <p>
     * Code sample:
     * <pre>
     * [javadoc]
     * public CoverageType getNumOfCoverageTypesInternal() {
     *     int numOf = 0;
     *     numOf += getNumOfCollisionCoverages();
     *     numOf += getNumOfTplCoverages();
     *     return numOf;
     * }
     * </pre>
     */
    private void generateMethodGetNumOfRelatedProductCmptsInternal(IProductCmptTypeRelation containerRelation, List implRelations, JavaCodeFragmentBuilder builder) throws CoreException {
        if (!containerRelation.isAbstractContainer()) {
            throw new IllegalArgumentException("Relation must be a container relation.");
        }
        builder.javaDoc("", ANNOTATION_GENERATED);
        String methodName = getMethodNameGetNumOfRelatedCmptsInternal(containerRelation);
        builder.signature(java.lang.reflect.Modifier.PRIVATE, "int", methodName, new String[]{}, new String[]{});
        builder.openBracket();
        builder.appendln("int num = 0;");
        IProductCmptType supertype = getProductCmptType().findSupertype();
        if (supertype!=null && !supertype.isAbstract()) {
            String methodName2 = interfaceBuilder.getMethodNameGetNumOfRelatedCmpts(containerRelation);
            builder.appendln("num += super." + methodName2 + "();");
        }
        for (Iterator it = implRelations.iterator(); it.hasNext();) {
            IProductCmptTypeRelation relation = (IProductCmptTypeRelation)it.next();
            builder.append("num += ");
            builder.append(interfaceBuilder.getMethodNameGetNumOfRelatedCmpts(relation));
            builder.append("();");
        }
        builder.appendln("return num;");
        builder.closeBracket();
    }
    
    /*
     * Returns the name of the internal method returning the number of referenced objects,
     * e.g. getNumOfCoveragesInternal()
     */
    private String getMethodNameGetNumOfRelatedCmptsInternal(IProductCmptTypeRelation relation) {
        return getLocalizedText(relation, "METHOD_GET_NUM_OF_INTERNAL_NAME", StringUtils.capitalise(relation.getTargetRolePlural()));
    }
    
    private void generateMethodGetCardinalityFor1ToManyRelation(IProductCmptTypeRelation relation, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("@inheritDoc", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetCardinalityForRelation(relation, methodsBuilder);
        String[][] params = interfaceBuilder.getParamGetCardinalityForRelation(relation);
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendOpenBracket();
        frag.append("if(");
        frag.append(params[0][0]);
        frag.append(" != null)");
        frag.appendOpenBracket();
        frag.append("return ");
        frag.append('(');
        frag.appendClassName(IntegerRange.class);
        frag.append(')');
        frag.append(getFieldNameCardinalityForRelation(relation));
        frag.append(".get(");
        frag.append(params[0][0]);
        frag.append(".getId());");
        frag.appendCloseBracket();
        frag.append("return null;");
        frag.appendCloseBracket();
        methodsBuilder.append(frag);
    }

    public String getFieldNameCardinalityForRelation(IProductCmptTypeRelation relation) throws CoreException{
        return getLocalizedText(relation, "FIELD_CARDINALITIES_FOR_NAME", relation.findPolicyCmptTypeRelation().getTargetRoleSingular());
    }
    
    private void generateFieldCardinalityForRelation(
            IProductCmptTypeRelation relation, JavaCodeFragmentBuilder fieldsBuilder) throws CoreException{
        appendLocalizedJavaDoc("FIELD_CARDINALITIES_FOR", relation.findPolicyCmptTypeRelation().getTargetRoleSingular(), relation, fieldsBuilder);
        JavaCodeFragment expression = new JavaCodeFragment();
        expression.append(" new ");
        expression.appendClassName(HashMap.class);
        expression.append("(0);");
        fieldsBuilder.varDeclaration(Modifier.PRIVATE, Map.class, getFieldNameCardinalityForRelation(relation), expression);
    }
    
    private void generateMethodGetRangeFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetRangeFor(a, helper, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameRangeFor(a));
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameRangeFor(IAttribute a){
        return getLocalizedText(a, "FIELD_RANGE_FOR_NAME", StringUtils.capitalise(a.getName()));
    }
    
    private void generateFieldRangeFor(IAttribute a, DatatypeHelper helper, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_RANGE_FOR", a.getName(), a, memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, helper.getRangeJavaClassName(), getFieldNameRangeFor(a)); 
    }

    private void generateMethodGetAllowedValuesFor(IAttribute a, Datatype datatype, JavaCodeFragmentBuilder methodsBuilder) throws CoreException{
        methodsBuilder.javaDoc("{@inheritDoc}", ANNOTATION_GENERATED);
        interfaceBuilder.generateSignatureGetAllowedValuesFor(a, datatype, methodsBuilder);
        JavaCodeFragment body = new JavaCodeFragment();
        body.appendOpenBracket();
        body.append("return ");
        body.append(getFieldNameAllowedValuesFor(a));
        body.appendln(';');
        body.appendCloseBracket();
        methodsBuilder.append(body);
    }
    
    public String getFieldNameAllowedValuesFor(IAttribute a){
        return getLocalizedText(a, "FIELD_ALLOWED_VALUES_FOR_NAME", StringUtils.capitalise(a.getName()));
    }
    
    private void generateFieldAllowedValuesFor(IAttribute a, JavaCodeFragmentBuilder memberVarBuilder){
        appendLocalizedJavaDoc("FIELD_ALLOWED_VALUES_FOR", a.getName(), a, memberVarBuilder);
        memberVarBuilder.varDeclaration(Modifier.PRIVATE, EnumValueSet.class, getFieldNameAllowedValuesFor(a)); 
    }

    class GetClassModifierFunction extends PolicyCmptTypeHierarchyVisitor {

        public GetClassModifierFunction(int modifier) {
            super();
            this.modifier = modifier;
        }
        
        private int modifier;
        
        /**
         * {@inheritDoc}
         */
        protected boolean visit(IPolicyCmptType type) {
            IAttribute[] attributes = type.getAttributes();
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].isDerived() && attributes[i].isProductRelevant()) {
                    // note: type can't be an instanceof ProductCmptType, as derived or computed policy cmpt type attributes
                    // aren't  not product cmpt attributes!
                    modifier = modifier | Modifier.ABSTRACT;
                    return false;
                }
            }
            return true;
        }
        
        public int getModifier() {
            return modifier;
        }
        
    }
}
