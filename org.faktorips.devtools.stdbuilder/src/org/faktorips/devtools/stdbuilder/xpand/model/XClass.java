/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashSet;

import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;

public abstract class XClass extends AbstractGeneratorModelNode {

    /**
     * The default java class name provider used by this class.
     * <p>
     * Never use this constant except in the method {@link #getJavaClassNameProvider()} because
     * {@link #getBaseSuperclassName()} may be overwritten in subclasses
     */
    private final IJavaClassNameProvider javaClassNameProvider;

    public XClass(IIpsObject ipsObject, GeneratorModelContext context, ModelService modelService) {
        super(ipsObject, context, modelService);
        javaClassNameProvider = createJavaClassNamingProvider(context.isGeneratePublishedInterfaces());
    }

    public abstract boolean isValidForCodeGeneration();

    public static IJavaClassNameProvider createJavaClassNamingProvider(boolean generatePublishedInterface) {
        return new DefaultJavaClassNameProvider(generatePublishedInterface);
    }

    /**
     * Getting the {@link IJavaClassNameProvider} providing the java class name generated for this
     * {@link XClass}
     * 
     * @return The {@link IJavaClassNameProvider} to get the names of the generated java classes for
     *         this {@link XClass}
     */
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    public JavaClassNaming getJavaClassNaming() {
        return getContext().getJavaClassNaming();
    }

    public String getFileName(BuilderAspect aspect) {
        return getJavaClassNaming().getRelativeJavaFile(getIpsObjectPartContainer().getIpsSrcFile(), aspect,
                getJavaClassNameProvider()).toOSString();
    }

    @Override
    public IIpsObject getIpsObjectPartContainer() {
        return (IIpsObject)super.getIpsObjectPartContainer();
    }

    /**
     * If the builder is configured to generate published interfaces, this method returns the name
     * of the published interface. Else the name of the implementation class is returned.
     */
    public String getPublishedInterfaceName() {
        return addImport(getSimpleName(BuilderAspect.getValue(isGeneratePublishedInterfaces())));
    }

    public String getSimpleName(BuilderAspect aspect) {
        return addImport(getQualifiedName(aspect));
    }

    public String getQualifiedName(BuilderAspect aspect) {
        return getJavaClassNaming().getQualifiedClassName(getIpsObjectPartContainer(), aspect,
                getJavaClassNameProvider());
    }

    public String getPackageName(BuilderAspect aspect) {
        return getJavaClassNaming().getPackageName(getIpsObjectPartContainer().getIpsSrcFile(), aspect,
                getJavaClassNameProvider());
    }

    /**
     * Returns the unqualified name of the base superclass that is used when the IPS type does not
     * have a super type. The qualified name must be added to the import declarations
     * 
     * @see #addImport(Class)
     * 
     * @return The unqualified name of the base superclass with added import statement.
     */
    protected abstract String getBaseSuperclassName();

    /**
     * Returns whether or not the published interface for this class extends other interfaces.
     */
    public boolean isExtendsInterface() {
        return !getExtendedInterfaces().isEmpty();
    }

    /**
     * Returns all interfaces the (generated) published interface for this class extends.
     * <p>
     * This method always return {@link #getExtendedOrImplementedInterfaces()} and maybe some
     * additional interfaces only need for published interfaces to be extended.
     */
    public abstract LinkedHashSet<String> getExtendedInterfaces();

    /**
     * Interfaces returned by this method are extended by published interfaces if we do generate
     * published interfaces but need to be implemented by the implementation if we do not generate
     * published interfaces.
     * 
     */
    protected abstract LinkedHashSet<String> getExtendedOrImplementedInterfaces();

    /**
     * Returns whether or not the generated class implements interfaces.
     */
    public boolean isImplementsInterface() {
        return !getImplementedInterfaces().isEmpty();
    }

    /**
     * Returns all interfaces the generated class implements.
     */
    public abstract LinkedHashSet<String> getImplementedInterfaces();

}
