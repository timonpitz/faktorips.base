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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesGenerator;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.MethodParameter;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.util.StringUtil;

public class XValidationRule extends AbstractGeneratorModelNode {

    public XValidationRule(IValidationRule validationRule, GeneratorModelContext context, ModelService modelService) {
        super(validationRule, context, modelService);
    }

    @Override
    public IValidationRule getIpsObjectPartContainer() {
        return (IValidationRule)super.getIpsObjectPartContainer();
    }

    public IValidationRule getValidationRule() {
        return getIpsObjectPartContainer();
    }

    public boolean isConfigured() {
        return getValidationRule().isConfigurableByProductComponent();
    }

    public boolean isContainsReplacementParameters() {
        return !getValidationRule().getMessageText().getReplacementParameters().isEmpty();
    }

    public LinkedHashSet<String> getReplacementParameters() {
        return convertToJavaParameters(getValidationRule().getMessageText().getReplacementParameters());
    }

    public String getMethodNameExecRule() {
        return StringUtils.uncapitalize(getName());
    }

    public String getMethodNameCreateMessage() {
        return "createMessageForRule" + StringUtils.capitalize(getName());
    }

    public List<MethodParameter> getCreateMessageParameters() {
        List<MethodParameter> result = new ArrayList<MethodParameter>();
        result.add(new MethodParameter(addImport(IValidationContext.class), "context"));
        for (String replacementParameter : getReplacementParameters()) {
            result.add(new MethodParameter(addImport(Object.class), replacementParameter));
        }
        return result;
    }

    public String getValidateMessageBundleName() {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)getIpsObjectPartContainer().getIpsSrcFile()
                .getIpsPackageFragment().getRoot().getIpsObjectPathEntry();
        return getContext().getValidationMessageBundleBaseName(entry);
    }

    public String getValidationMessageKey() {
        return ValidationRuleMessagesGenerator.getMessageKey(getIpsObjectPartContainer());
    }

    public String getMessageCode() {
        String upperCaseName = getName();
        if (isGenerateSeparatedCamelCase()) {
            upperCaseName = StringUtil.camelCaseToUnderscore(upperCaseName, false);
        }
        upperCaseName = upperCaseName.toUpperCase();
        return "MSG_CODE_" + upperCaseName;
    }

    public String getSeverityConstant() {
        JavaCodeFragment codeFragment = getValidationRule().getMessageSeverity().getJavaSourcecode();
        addImport(codeFragment.getImportDeclaration());
        return codeFragment.getSourcecode();
    }

    /**
     * Converts the parameters found in the list of parameters to be java compatible variable names.
     * If any parameter is no valid java identifier we add the prefix 'p' (for parameter). If there
     * is already another parameter with this name we add as much 'p' as needed to be unique.
     * 
     */
    LinkedHashSet<String> convertToJavaParameters(LinkedHashSet<String> parameters) {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (final String parameterName : parameters) {
            if (!Character.isJavaIdentifierStart(parameterName.charAt(0))) {
                String javaIdent = "p" + parameterName;
                while (parameters.contains(javaIdent)) {
                    javaIdent = "p" + javaIdent;
                }
                result.add(javaIdent);
            } else {
                result.add(parameterName);
            }
        }
        return result;
    }

}
