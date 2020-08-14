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

import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.devtools.stdbuilder.xmodel.XType;

public class XTypeMapperClassNameProvider {
    private final MapperJavaClassNameProvider mapperNameProvider;
    private final DefaultJavaClassNameProvider defNameProvider;
    private XType type;
    private IIpsSrcFile ipsSrcFile;

    public XTypeMapperClassNameProvider(XType type) {
        mapperNameProvider = new MapperJavaClassNameProvider();
        defNameProvider = new DefaultJavaClassNameProvider(
                type.getGeneratorConfig().isGeneratePublishedInterfaces(type.getIpsProject()));
        this.type = type;
        this.ipsSrcFile = type.getIpsObjectPartContainer().getIpsSrcFile();
    }

    public MapperJavaClassNameProvider getMapperNameProvider() {
        return mapperNameProvider;
    }

    public DefaultJavaClassNameProvider getDefNameProvider() {
        return defNameProvider;
    }

    public String getName() {
        return QNameUtil.getUnqualifiedName(mapperNameProvider.getImplClassName(ipsSrcFile));
    }

    public String getTypeName() {
        return QNameUtil.getUnqualifiedName(mapperNameProvider.getDeclClassName(ipsSrcFile));
    }
}
