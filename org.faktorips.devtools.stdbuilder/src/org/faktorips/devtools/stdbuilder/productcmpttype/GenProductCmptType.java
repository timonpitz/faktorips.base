/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractProductCmptTypeBuilder;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationTo1;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationToMany;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProdAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

public class GenProductCmptType extends GenType {

    private Map generatorsByPart = new HashMap();
    private List genProdAttributes = new ArrayList();
    private List genProdAssociations = new ArrayList();
    private List genMethods = new ArrayList();

    public GenProductCmptType(IProductCmptType productCmptType, StandardBuilderSet builderSet,
            LocalizedStringsSet stringsSet) throws CoreException {
        super(productCmptType, builderSet, stringsSet);
        createGeneratorsForProdAttributes();
        createGeneratorsForProdAssociations();
        createGeneratorsForMethods();
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getType();
    }

    /**
     * {@inheritDoc}
     */
    public String getUnqualifiedClassNameForProductCmptTypeGen(boolean forInterface) throws CoreException {
        if (forInterface) {
            String name = getType().getName() + getAbbreviationForGenerationConcept();
            return getJavaNamingConvention().getPublishedInterfaceName(name);
        }
        String generationAbb = getAbbreviationForGenerationConcept();
        return getJavaNamingConvention().getImplementationClassName(getType().getName() + generationAbb);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IIpsProject ipsProject, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        IPolicyCmptType pcType = getProductCmptType().findPolicyCmptType(ipsProject);
        String[] replacements = new String[] { getNameForGenerationConcept(), getType().getName(),
                pcType != null ? pcType.getName() : "missing" };
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT_GENERATION", replacements, methodsBuilder);
        generateSignatureGetProductCmptGeneration(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Returns the name of the method to access the product component generation, e.g.
     * getMotorProductGen
     */
    public String getMethodNameGetProductCmptGeneration() throws CoreException {
        String[] replacements = new String[] { getType().getName(), getAbbreviationForGenerationConcept(),
                getNameForGenerationConcept() };
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_GENERATION_NAME", replacements);
    }

    /**
     * Returns the name of the method to set the product component, e.g. setMotorProduct
     */
    public String getMethodNameSetProductCmpt() throws CoreException {
        return getLocalizedText("METHOD_SET_PRODUCTCMPT_NAME", getType().getName());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    public void generateSignatureGetProductCmptGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetProductCmptGeneration();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, getQualifiedClassNameForProductCmptTypeGen(true),
                methodName, new String[0], new String[0]);
    }

    /**
     * Returns the name of the method to access the product component, e.g. getMotorProduct
     */
    public String getMethodNameGetProductCmpt() throws CoreException {
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_NAME", getProductCmptType().getName());
    }

    private void createGeneratorsForProdAttributes() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenProdAttribute.class);
        IProductCmptTypeAttribute[] attrs = getProductCmptType().getProductCmptTypeAttributes();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isValid()) {
                GenProdAttribute generator = new GenProdAttribute(this, attrs[i], stringsSet);
                genProdAttributes.add(generator);
                generatorsByPart.put(attrs[i], generator);
            }
        }
    }

    private void createGeneratorsForProdAssociations() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenProdAssociation.class);
        IProductCmptTypeAssociation[] ass = getProductCmptType().getProductCmptTypeAssociations();
        for (int i = 0; i < ass.length; i++) {
            if (ass[i].isValid()) {
                GenProdAssociation generator = createGenerator(ass[i], stringsSet);
                genProdAssociations.add(generator);
                generatorsByPart.put(ass[i], generator);
            }
        }
    }

    private void createGeneratorsForMethods() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenProdMethod.class);
        IProductCmptTypeMethod[] methods = getProductCmptType().getProductCmptTypeMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isValid()) {
                GenProdMethod generator = new GenProdMethod(this, methods[i], stringsSet);
                genMethods.add(generator);
                generatorsByPart.put(methods[i], generator);
            }
        }
    }

    private GenProdAssociation createGenerator(IProductCmptTypeAssociation association, LocalizedStringsSet stringsSet)
            throws CoreException {
        if (association.is1ToMany()) {
            return new GenProdAssociationToMany(this, association, stringsSet);
        }
        return new GenProdAssociationTo1(this, association, stringsSet);
    }

    public GenProdAttribute getGenerator(IProductCmptTypeAttribute a) throws CoreException {
        GenProdAttribute generator = (GenProdAttribute)generatorsByPart.get(a);
        if (null != generator) {
            return generator;
        }
        // if the associations policy component type is not this type but one in the super type
        // hierarchy of this type
        if (!a.getProductCmptType().equals(getProductCmptType())) {
            GenProductCmptType superTypeGenerator = getBuilderSet().getGenerator(a.getProductCmptType());
            return superTypeGenerator.getGenerator(a);
        }
        return generator;
    }

    public GenProdMethod getGenerator(IProductCmptTypeMethod method) throws CoreException {
        GenProdMethod generator = (GenProdMethod)generatorsByPart.get(method);
        if(generator != null){
            return generator;
        }
        // if the associations policy component type is not this type but one in the super type
        // hierarchy of this type
        if (!method.getProductCmptType().equals(getProductCmptType())) {
            GenProductCmptType superTypeGenerator = getBuilderSet().getGenerator(method.getProductCmptType());
            return superTypeGenerator.getGenerator(method);
        }
        return null;
    }

    public GenProdAssociation getGenerator(IProductCmptTypeAssociation a) throws CoreException {
        GenProdAssociation generator = (GenProdAssociation)generatorsByPart.get(a);
        if(generator != null){
            return generator;
        }
        // if the associations policy component type is not this type but one in the super type
        // hierarchy of this type
        if (!a.getProductCmptType().equals(getProductCmptType())) {
            GenProductCmptType superTypeGenerator = getBuilderSet().getGenerator(a.getProductCmptType());
            return superTypeGenerator.getGenerator(a);
        }
        return null;
    }

    public Iterator getGenProdAttributes() {
        return genProdAttributes.iterator();
    }

    public JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()."
                + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
    }

    public String getMethodNameGetGeneration() throws CoreException {
        IChangesOverTimeNamingConvention convention = getProductCmptType().getIpsProject()
                .getChangesInTimeNamingConventionForGeneratedCode();
        String generationConceptName = convention.getGenerationConceptNameSingular(getProductCmptType().getIpsProject()
                .getGeneratedJavaSourcecodeDocumentationLanguage());
        String generationConceptAbbreviation = convention.getGenerationConceptNameAbbreviation(getProductCmptType()
                .getIpsProject().getGeneratedJavaSourcecodeDocumentationLanguage());
        return getLocalizedText("METHOD_GET_GENERATION_NAME", new String[] { getProductCmptType().getName(),
                generationConceptAbbreviation, generationConceptName });
    }

    public String getQualifiedClassNameForProductCmptTypeGen(boolean forInterface) throws CoreException {
        return getQualifiedName(forInterface)
                + getChangesInTimeNamingConvention().getGenerationConceptNameAbbreviation(
                        getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IProductGen getGeneration(Calendar effectiveDate)
     * </pre>
     */
    void generateSignatureGetGeneration(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String generationInterface = getQualifiedClassNameForProductCmptTypeGen(true);
        String methodName = getMethodNameGetGeneration();
        String paramName = getVarNameEffectiveDate();
        methodsBuilder.signature(Modifier.PUBLIC, generationInterface, methodName, new String[] { paramName },
                new String[] { Calendar.class.getName() });
    }

    /**
     * Returns the variable or parameter name for the effetiveDate.
     * 
     * @param element An isp element that gives access to the ips project.
     * @see org.faktorips.devtools.core.builder.AbstractProductCmptTypeBuilder#getVarNameEffectiveDate
     */
    public String getVarNameEffectiveDate() {
        return AbstractProductCmptTypeBuilder.getVarNameEffectiveDate(getProductCmptType());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IMotorProduct getMotorProduct()
     * </pre>
     */
    public void generateSignatureGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String returnType = getQualifiedName(true);
        String methodName = getMethodNameGetProductCmpt();
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults)
     * </pre>
     */
    public void generateSignatureSetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameSetProductCmpt();
        String[] paramTypes = new String[] { getQualifiedName(true), "boolean" };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName,
                getMethodParamNamesSetProductCmpt(), paramTypes);
    }

    /**
     * Returns the method parameters for the method: setProductCmpt.
     */
    public String[] getMethodParamNamesSetProductCmpt() throws CoreException {
        return new String[] { StringUtils.uncapitalize(getType().getName()), "initPropertiesWithConfiguratedDefaults" };
    }

}
