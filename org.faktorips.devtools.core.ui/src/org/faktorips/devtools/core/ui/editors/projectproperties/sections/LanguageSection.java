/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.internal.model.ipsproject.SupportedLanguage;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;

public class LanguageSection {
    public IIpsProjectProperties iIpsProjectProperties;
    private Section section;
    private int style = ExpandableComposite.TITLE_BAR;
    private GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

    public LanguageSection(IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit) {
        this.iIpsProjectProperties = iIpsProjectProperties;
        section = toolkit.getFormToolkit().createSection(parent, style);
        section.setLayoutData(layoutData);
        section.setText("aaa");
        create(parent, toolkit);
    }

    public LanguageCompsite create(Composite parent, UIToolkit toolkit) {
        return new LanguageCompsite(iIpsProjectProperties, parent, toolkit);
    }

    public class LanguageCompsite extends Buttons {
        private Set<ISupportedLanguage> input;

        public LanguageCompsite(IIpsProjectProperties iIpsProjectProperties, Composite parent, boolean canCreate,
                boolean canEdit, boolean canDelete, boolean canMove, boolean showEditButton, UIToolkit toolkit) {
            super(iIpsProjectProperties, parent, canCreate, canEdit, canDelete, canMove, showEditButton, toolkit,
                    ExpandableComposite.TITLE_BAR);
            initControls(toolkit);
            // setText(Messages.Language_title);
        }

        public LanguageCompsite(IIpsProjectProperties iIpsProjectProperties, Composite members, UIToolkit toolkit) {
            this(iIpsProjectProperties, members, true, false, true, true, false, toolkit);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ArrayContentProvider();
        }

        @Override
        protected EditDialog createEditDialog(Object object, Shell shell) throws CoreException {
            return new LanguageEditDialog(shell, Messages.LanguageEditDialog_title);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected void fillViewer() {
            // input = iIpsProjectProperties.getSupportedLanguages();
            input = new HashSet<ISupportedLanguage>();
            ISupportedLanguage selection = new SupportedLanguage();
            input.add(selection);
            viewer.setInput(input);
        }

        @Override
        public void deleteItem() {
            ISupportedLanguage selection = getSelectedPart();
            input.remove(selection);

        }

        public final ISupportedLanguage getSelectedPart() {
            return (ISupportedLanguage)getSelectedObject();
        }
    }
}