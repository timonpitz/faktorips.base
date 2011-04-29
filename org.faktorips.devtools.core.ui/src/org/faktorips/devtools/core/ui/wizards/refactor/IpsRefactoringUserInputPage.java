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

package org.faktorips.devtools.core.ui.wizards.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class providing common functionality for {@link UserInputWizardPage}s used by
 * Faktor-IPS refactorings.
 * 
 * @author Alexander Weickmann
 */
abstract class IpsRefactoringUserInputPage extends UserInputWizardPage {

    /** {@link IIpsElement} to be refactored. */
    private IIpsElement ipsElement;

    /** {@link UIToolkit} to create UI elements with. */
    private final UIToolkit uiToolkit;

    /**
     * @param pageName A name for this user input page
     * @param ipsElement {@link IIpsElement} to be refactored
     * 
     * @throws NullPointerException If any parameter is null
     */
    IpsRefactoringUserInputPage(IIpsElement ipsElement, String pageName) {
        super(pageName);
        ArgumentCheck.notNull(ipsElement);

        this.ipsElement = ipsElement;
        uiToolkit = new UIToolkit(null);

        setPromptMessage();
    }

    /**
     * Subclass implementation responsible for setting the prompt message.
     * <p>
     * This operation is called by the constructor.
     */
    // TODO AW: Overridable methods should not be called by constructor
    protected abstract void setPromptMessage();

    /**
     * Returns the name describing the {@link IIpsElement} to be refactored.
     */
    // TODO AW: This should be moved to the core model
    protected final String getIpsElementName() {
        String ipsElementName = ""; //$NON-NLS-1$
        if (ipsElement instanceof IAttribute) {
            ipsElementName = Messages.ElementNames_Attribute;
        } else if (ipsElement instanceof IMethod) {
            ipsElementName = Messages.ElementNames_Method;
        } else if (ipsElement instanceof IAssociation) {
            ipsElementName = Messages.ElementNames_Association;
        } else if (ipsElement instanceof IType) {
            ipsElementName = Messages.ElementNames_Type;
        } else if (ipsElement instanceof IEnumLiteralNameAttributeValue) {
            ipsElementName = Messages.ElementNames_EnumLiteralNameAttributeValue;
        }
        return ipsElementName;
    }

    /**
     * Operation that should be called when any user input has changed, triggers validation.
     */
    protected final void userInputChanged() {
        try {
            boolean userInputValid = validateUserInput();
            setPageComplete(userInputValid);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the user input, manages page messages and calls a subclass implementation.
     * <p>
     * Returns true if valid, false otherwise.
     */
    private final boolean validateUserInput() throws CoreException {
        resetPageMessages();

        RefactoringStatus status = new RefactoringStatus();
        validateUserInputThis(status);
        evaluateValidation(status);

        return !(status.hasError());
    }

    /**
     * Subclass implementation responsible for validating the user input.
     * 
     * @param status {@link RefactoringStatus} to add messages to
     */
    protected abstract void validateUserInputThis(RefactoringStatus status) throws CoreException;

    /**
     * Evaluates the given {@link RefactoringStatus} by setting appropriate page messages.
     */
    protected final void evaluateValidation(RefactoringStatus status) {
        for (RefactoringStatusEntry entry : status.getEntries()) {
            switch (entry.getSeverity()) {
                case RefactoringStatus.ERROR:
                case RefactoringStatus.FATAL:
                    setErrorMessage(entry.getMessage());
                    break;
                case RefactoringStatus.WARNING:
                    setMessage(entry.getMessage(), WARNING);
                    break;
                case RefactoringStatus.INFO:
                    setMessage(entry.getMessage(), INFORMATION);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Resets any messages to the default, which means the prompt message is shown.
     */
    protected final void resetPageMessages() {
        setErrorMessage(null);
        setMessage(null, WARNING);
        setMessage(null, INFORMATION);
        setPromptMessage();
    }

    /**
     * Returns the {@link IIpsElement} to be refactored.
     */
    protected final IIpsElement getIpsElement() {
        return ipsElement;
    }

    /**
     * Returns the {@link UIToolkit} to create new UI elements with.
     */
    protected final UIToolkit getUiToolkit() {
        return uiToolkit;
    }

}
