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

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class is loaded by the Faktor-IPS move refactoring to participate in this process by moving
 * the Java source code.
 * <p>
 * This is accomplished by successively calling JDT refactorings on the <tt>IJavaElement</tt>
 * generated by the code generator for the <tt>IIpsElement</tt> to be refactored.
 * 
 * @author Alexander Weickmann
 */
public class MoveRefactoringParticipant extends org.eclipse.ltk.core.refactoring.participants.MoveParticipant {

    /** A helper providing shared standard builder refactoring functionality. */
    private RefactoringParticipantHelper refactoringHelper;

    /** Creates a <tt>MoveRefactoringParticipant</tt>. */
    public MoveRefactoringParticipant() {
        refactoringHelper = new MoveParticipantHelper();
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        return refactoringHelper.checkConditions(pm, context);
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return refactoringHelper.createChange(pm);
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize(element);
    }

    @Override
    public String getName() {
        return "StandardBuilder Move Participant";
    }

    /** The <tt>RefactoringParticipantHelper</tt> for this participant. */
    private final class MoveParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected void createChangeThis(IJavaElement originalJavaElement,
                IJavaElement newJavaElement,
                IProgressMonitor pm) throws CoreException, OperationCanceledException {

            if (originalJavaElement.getElementType() == IJavaElement.TYPE
                    && newJavaElement.getElementType() == IJavaElement.TYPE) {

                // Rename the Java type if necessary.
                if (!(newJavaElement.getElementName().equals(originalJavaElement.getElementName()))) {
                    renameJavaElement(originalJavaElement, newJavaElement.getElementName(), getArguments()
                            .getUpdateReferences(), pm);
                }

                moveJavaElement((IType)originalJavaElement, ((IType)newJavaElement).getPackageFragment(), pm);
            }
        }

        /**
         * Moves the given Java <tt>IType</tt> to the given destination Java
         * <tt>IPackageFragment</tt>.
         */
        private void moveJavaElement(IType javaType, IPackageFragment targetPackageFragment, final IProgressMonitor pm)
                throws CoreException {

            RefactoringContribution moveContribution = RefactoringCore
                    .getRefactoringContribution(IJavaRefactorings.MOVE);
            MoveDescriptor moveDescriptor = (MoveDescriptor)moveContribution.createDescriptor();
            moveDescriptor.setMoveResources(new IFile[0], new IFolder[0], new ICompilationUnit[] { javaType
                    .getCompilationUnit() });
            moveDescriptor.setDestination(targetPackageFragment);
            moveDescriptor.setProject(targetPackageFragment.getJavaProject().getElementName());
            moveDescriptor.setUpdateReferences(getArguments().getUpdateReferences());

            performRefactoring(moveDescriptor, pm);
        }

        @Override
        protected boolean initializeNewJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet) {
            if (ipsElement instanceof IPolicyCmptType) {
                IPolicyCmptType policyCmptType = (IPolicyCmptType)ipsElement;
                initNewJavaElements(policyCmptType, (LocationDescriptor)getArguments().getDestination(), builderSet);

            } else if (ipsElement instanceof IProductCmptType) {
                IProductCmptType productCmptType = (IProductCmptType)ipsElement;
                initNewJavaElements(productCmptType, (LocationDescriptor)getArguments().getDestination(), builderSet);

            } else {
                return false;
            }

            return true;
        }

    }

}
