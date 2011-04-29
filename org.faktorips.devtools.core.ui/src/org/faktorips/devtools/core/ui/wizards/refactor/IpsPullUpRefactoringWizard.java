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
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsElement;

public class IpsPullUpRefactoringWizard extends IpsRefactoringWizard {

    public IpsPullUpRefactoringWizard(Refactoring refactoring, IIpsElement ipsElement) {
        super(refactoring, ipsElement, WIZARD_BASED_USER_INTERFACE | NO_PREVIEW_PAGE);
    }

    @Override
    protected void addUserInputPages() {
        addPage(new PullUpPage(getIpsElement()));
    }

    @Override
    public boolean needsPreviousAndNextButtons() {
        return false;
    }

    private static class PullUpPage extends IpsRefactoringUserInputPage {

        PullUpPage(IIpsElement ipsElement) {
            super(ipsElement, "PullUpPage"); //$NON-NLS-1$
        }

        @Override
        public void createControl(Composite parent) {
            Composite controlComposite = getUiToolkit().createGridComposite(parent, 1, false, false);
            setControl(controlComposite);
        }

        @Override
        protected void setPromptMessage() {
            // Nothing to do
        }

        @Override
        protected void validateUserInputThis(RefactoringStatus status) throws CoreException {
            // Nothing to do
        }

    }

}
