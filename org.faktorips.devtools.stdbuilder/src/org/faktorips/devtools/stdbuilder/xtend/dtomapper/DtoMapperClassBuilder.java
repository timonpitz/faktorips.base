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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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
        if (ipsSrcFile.exists() && IpsObjectType.POLICY_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType())) {
            IIpsObject ipsObject = ipsSrcFile.getIpsObject();
            if (ipsObject != null
                    && ipsObject.isExtPropertyDefinitionAvailable("org.faktorips.devtools.stdbuilder.DTO Class")
                    && StringUtils.isNotBlank(
                            (String)ipsObject.getExtPropertyValue("org.faktorips.devtools.stdbuilder.DTO Class"))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
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

        try {
            if (isBuilderFor(ipsObjectPartContainer.getIpsSrcFile())) {
                return true;
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
