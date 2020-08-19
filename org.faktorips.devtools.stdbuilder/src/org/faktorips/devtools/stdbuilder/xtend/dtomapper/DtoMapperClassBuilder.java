/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xtend.dtomapper;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.dtomapper.XDtoMapper;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendTypeBuilder;
import org.faktorips.devtools.stdbuilder.xtend.dtomapper.template.DtoMapperTmpl;
import org.faktorips.util.LocalizedStringsSet;

public class DtoMapperClassBuilder extends XtendTypeBuilder<XDtoMapper> {

    private IJavaClassNameProvider javaClassNameProvider;

    public DtoMapperClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService,
                new LocalizedStringsSet(DtoMapperClassBuilder.class));
        javaClassNameProvider = new MapperJavaClassNameProvider();
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        if (!generatesInterface()) {
            return DtoMapperTmpl.generate(getGeneratorModelRoot(ipsObject));
        } else {
            return null;
        }
    }

    @Override
    protected Class<XDtoMapper> getGeneratorModelRootType() {
        return XDtoMapper.class;
    }

    @Override
    public boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer) {
        /*
         * boolean isGeneratingArtifacts = false; try { if
         * (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile()) && ipsObjectPartContainer
         * .isExtPropertyDefinitionAvailable("org.faktorips.devtools.stdbuilder.DTO Class") &&
         * !ipsObjectPartContainer.getExtPropertyValue("org.faktorips.devtools.stdbuilder.DTO Class"
         * ) .toString().isEmpty()) { isGeneratingArtifacts = true; } else { isGeneratingArtifacts =
         * false; } } catch (CoreException e) { throw new CoreRuntimeException(e); }
         * System.out.println("isGeneratingArtifacts = " + isGeneratingArtifacts); return
         * isGeneratingArtifacts;
         */
        return false;
    }

    @Override
    protected IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer) {
        IIpsObject ipsObject = ipsObjectPartContainer.getIpsObject();
        if (ipsObject instanceof IPolicyCmptType) {
            return ipsObject;
        } else if (ipsObject instanceof IProductCmptType) {
            IProductCmptType productCmptType = (IProductCmptType)ipsObject;
            if (productCmptType.isConfigurationForPolicyCmptType()) {
                return productCmptType.findPolicyCmptType(productCmptType.getIpsProject());
            }
        }
        return null;
    }

}
