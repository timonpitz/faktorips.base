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

package org.faktorips.devtools.core.ui.views.modeloverview;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class IpsModelOverviewLabelProvider extends LabelProvider {

    public IpsModelOverviewLabelProvider() {
        super();
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof ComponentNode) {
            ComponentNode node = (ComponentNode)element;

            IAdaptable adaptable = node.getValue();
            Image result = IpsUIPlugin.getImageHandling().getImage(adaptable);
            if (result != null) {
                return result;
            }
        } else if (element instanceof CompositeNode) {
            return IpsUIPlugin.getImageHandling().getSharedImage("AssociationType-Aggregation.gif", true); //$NON-NLS-1$
        } else if (element instanceof SubtypeNode) {
            return IpsUIPlugin.getImageHandling().getSharedImage("over_co.gif", true); //$NON-NLS-1$
        }
        return super.getImage(element);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ComponentNode) {
            ComponentNode node = (ComponentNode)element;
            return node.getValue().getName();
        } else if (element instanceof CompositeNode) {
            return ""; //$NON-NLS-1$
        } else if (element instanceof SubtypeNode) {
            return ""; //$NON-NLS-1$
        }
        return super.getText(element);
    }
}
