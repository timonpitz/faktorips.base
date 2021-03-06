/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;

/**
 * ProductComponent for testing purposes.
 *
 * @author Jan Ortmann
 */
public class TestProductComponent extends ProductComponent {

    public TestProductComponent(IRuntimeRepository repository, String id, String productKindId, String versionId) {
        super(repository, id, productKindId, versionId);
    }

    protected ProductComponentGeneration createGeneration() {
        return new TestProductCmptGeneration(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IConfigurableModelObject createPolicyComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isChangingOverTime() {
        return true;
    }

}
