/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xmodel.dtomapper;

import java.util.Set;

import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.dtomapper.XTypeMapperClassNameProvider;

public class XDtoMapper extends XPolicyCmptClass {

    private XTypeMapperClassNameProvider nameProvider;

    public XDtoMapper(IPolicyCmptType policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
        this.nameProvider = new XTypeMapperClassNameProvider(this);
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return nameProvider.getMapperNameProvider();
    }

    /**
     * {@inheritDoc} No import statement is added. For import statement, see
     * {@link #getImplClassName()}
     * 
     * @return Name of the builder
     */
    @Override
    public String getName() {
        return nameProvider.getName();
    }

    /**
     * No import statement added
     * 
     * @return name of the policy class
     */
    public String getPolicyName() {
        return nameProvider.getTypeName();
    }

    @Override
    public Set<XPolicyAttribute> getAttributes() {
        return super.getAttributes();
    }

}
