/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.faktorips.runtime.internal.ProductComponent;

public class XProductCmptClass extends XProductClass {

    private static final boolean CHANGE_OVER_TIME = false;

    private final Set<XProductAttribute> attributes;

    private final Set<XProductAssociation> associations;

    private final Set<XDerivedUnionAssociation> derivedUnionAssociations;

    public XProductCmptClass(IProductCmptType ipsObjectPartContainer, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(ipsObjectPartContainer, modelContext, modelService);

        attributes = initNodesForParts(getProductAttributes(CHANGE_OVER_TIME), XProductAttribute.class);
        associations = initNodesForParts(getProductAssociations(CHANGE_OVER_TIME), XProductAssociation.class);
        derivedUnionAssociations = initNodesForParts(getProductDerivedUnionAssociations(CHANGE_OVER_TIME),
                XDerivedUnionAssociation.class);
    }

    @Override
    public Set<XProductAttribute> getAttributes() {
        return new CopyOnWriteArraySet<XProductAttribute>(attributes);
    }

    @Override
    public Set<XProductAssociation> getAssociations() {
        return new CopyOnWriteArraySet<XProductAssociation>(associations);
    }

    @Override
    public Set<XDerivedUnionAssociation> getDerivedUnionAssociations() {
        return new CopyOnWriteArraySet<XDerivedUnionAssociation>(derivedUnionAssociations);
    }

    public IProductCmptType getProductCmptType() {
        return getIpsObjectPartContainer();
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(ProductComponent.class);
    }

    public String getGetterMethodNameForGeneration() {
        IChangesOverTimeNamingConvention convention = getProductCmptType().getIpsProject()
                .getChangesInTimeNamingConventionForGeneratedCode();
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String generationConceptAbbreviation = convention.getGenerationConceptNameAbbreviation(locale);
        return "get" + getProductCmptType().getName() + generationConceptAbbreviation;
    }

}
