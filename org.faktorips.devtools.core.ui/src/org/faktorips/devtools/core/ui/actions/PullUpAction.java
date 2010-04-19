/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.wizards.refactor.PullUpRefactoringWizard;

public class PullUpAction extends IpsRefactoringAction {

    public PullUpAction(Shell shell, ISelectionProvider selectionProvider) {
        super(shell, selectionProvider);
        setText("Pull Up...");
    }

    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        Refactoring refactoring = ((IIpsElement)selected).getPullUpRefactoring();
        RefactoringWizard pullUpWizard = new PullUpRefactoringWizard(refactoring, (IIpsElement)selected);
        openWizard(pullUpWizard);
    }

}
