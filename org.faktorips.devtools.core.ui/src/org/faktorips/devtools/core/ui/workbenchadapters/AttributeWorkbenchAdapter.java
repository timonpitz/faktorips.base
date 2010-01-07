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

package org.faktorips.devtools.core.ui.workbenchadapters;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

public class AttributeWorkbenchAdapter extends IpsObjectPartWorkbenchAdapter {

    public static final String BASE_IMAGE = "AttributePublic.gif"; //$NON-NLS-1$ 

    @Override
    protected ImageDescriptor getImageDescriptor(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsObjectPart;
            String[] overlays = new String[4];

            if (attribute instanceof IPolicyCmptTypeAttribute
                    && ((IPolicyCmptTypeAttribute)attribute).isProductRelevant()) {
                overlays[1] = OverlayIcons.PRODUCT_OVR;
            }
            return IpsUIPlugin.getImageHandling().getSharedOverlayImage(BASE_IMAGE, overlays);
        }
        return null;
    }

    @Override
    public ImageDescriptor getDefaultImageDescriptor() {
        return IpsUIPlugin.getImageHandling().getSharedImageDescriptor(BASE_IMAGE, true);
    }

    @Override
    protected String getLabel(IIpsObjectPart ipsObjectPart) {
        if (ipsObjectPart instanceof IAttribute) {
            IAttribute attribute = (IAttribute)ipsObjectPart;
            String label = attribute.getName();
            if (attribute.isDerived()) {
                label = "/" + label;
            }
            if (!StringUtils.isEmpty(attribute.getDatatype())) {
                label += " : " + attribute.getDatatype();
            }
            return label;
        } else {
            return super.getLabel(ipsObjectPart);
        }
    }

}
