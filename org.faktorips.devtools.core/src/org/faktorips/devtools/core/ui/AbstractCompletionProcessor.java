package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;


/**
 * Abstract base class for ips completion processors.
 */
public abstract class AbstractCompletionProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {
    
    protected IIpsProject ipsProject;
    private String errorMessage;
    private boolean computeProposalForEmptyPrefix = false;

    public AbstractCompletionProcessor() {
    }
    
    public AbstractCompletionProcessor(IIpsProject ipsProject) {
    	this.ipsProject = ipsProject;
    }
    
    public void setIpsProject(IIpsProject project) {
        ipsProject = project;
    }
    
    /**
     * If true, the processor proposes all objects if the user has provided
     * no prefix to start with. If false, the processor won't generate a
     * prososal.
     */
    public void setComputeProposalForEmptyPrefix(boolean value) {
        computeProposalForEmptyPrefix = value;
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported."); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported."); //$NON-NLS-1$
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[0];
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
		return null; //no context
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeContextInformation(org.eclipse.jface.contentassist.IContentAssistSubjectControl, int)
     */
    public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
        return null;
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.contentassist.IContentAssistSubjectControl, int)
     */
    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		if (documentOffset == 0 && !computeProposalForEmptyPrefix) {
			return null;
		}
        if (ipsProject==null) {
            errorMessage = Messages.AbstractCompletionProcessor_msgNoProject;
            return null;
        }
		String input= contentAssistSubjectControl.getDocument().get();
        String prefix = input.substring(0, documentOffset);
        
        List result = new ArrayList(100);
        try {
            doComputeCompletionProposals(prefix, documentOffset, result);
        } catch (Exception e) {
            errorMessage = Messages.AbstractCompletionProcessor_msgInternalError;
            IpsPlugin.log(e);
            return null;
        }
        
		ICompletionProposal[] proposals = new ICompletionProposal[result.size()];
		result.toArray(proposals);
		return proposals;
    }
    
    protected abstract void doComputeCompletionProposals(
            String prefix, int documentOffset, List result) throws Exception; 


}
