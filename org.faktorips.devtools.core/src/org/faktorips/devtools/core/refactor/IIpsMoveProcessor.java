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

package org.faktorips.devtools.core.refactor;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * An <tt>IIpsMoveProcessor</tt> implements a specific Faktor-IPS "Move" refactoring.
 * 
 * @author Alexander Weickmann
 */
public interface IIpsMoveProcessor extends IIpsRefactoringProcessor {

    /**
     * Sets the target <tt>IIpsPackageFragment</tt>.
     * 
     * @param targetIpsPackageFragment The target <tt>IIpsPackageFragment</tt>.
     * 
     * @throws NullPointerException If <tt>targetIpsPackageFragment</tt> is <tt>null</tt>.
     */
    public void setTargetIpsPackageFragment(IIpsPackageFragment targetIpsPackageFragment);

    /**
     * Sets whether the runtime ID of {@link IProductCmpt} should be adapted.
     * 
     * @param adaptRuntimeId Flag indicating whether to adapt runtime IDs.
     */
    public void setAdaptRuntimeId(boolean adaptRuntimeId);

    /** Returns the target <tt>IIpsPackageFragment</tt>. */
    public IIpsPackageFragment getTargetIpsPackageFragment();

    /** Returns the element's original <tt>IIpsPackageFragment</tt>. */
    public IIpsPackageFragment getOriginalIpsPackageFragment();

    /**
     * Returns whether the runtime ID of {@link IProductCmpt} should be adapted.
     */
    public boolean isAdaptRuntimeId();

}
