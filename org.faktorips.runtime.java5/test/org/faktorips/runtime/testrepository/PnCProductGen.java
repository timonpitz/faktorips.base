/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime.testrepository;

import java.util.Map;

import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.w3c.dom.Element;

public class PnCProductGen extends ProductComponentGeneration {


    private Decimal taxRate;
    private Money fixedCosts;

    public PnCProductGen(ProductComponent productCmpt) {
        super(productCmpt);
    }

    public Decimal getTaxRate() {
        return taxRate;
    }
    
    public Money getFixedCosts() {
        return fixedCosts;
    }
    
    protected void doInitPropertiesFromXml(Map map) {
        Element taxRateElement = (Element)map.get("taxRate");
        taxRate = Decimal.valueOf(taxRateElement.getAttribute("value"));
        Element fixedCostsElement = (Element)map.get("fixedCosts");
        fixedCosts = Money.valueOf(fixedCostsElement.getAttribute("value"));
    }

    protected void doInitReferencesFromXml(Map map) {
    }

}
