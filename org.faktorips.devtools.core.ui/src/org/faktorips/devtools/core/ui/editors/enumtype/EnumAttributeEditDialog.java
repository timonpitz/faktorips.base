/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.ValueTypeMismatch;
import org.faktorips.devtools.core.refactor.IIpsRefactoring;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.InfoLabel;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.refactor.IpsRefactoringOperation;
import org.faktorips.util.StringUtil;

/**
 * Dialog to edit an <tt>IEnumAttribute</tt> of an <tt>IEnumType</tt>.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributeEditDialog extends IpsPartEditDialog2 {

    /** The <tt>IEnumAttribute</tt> being edited. */
    private IEnumAttribute enumAttribute;

    /** The extension property factory that may extend the controls. */
    private ExtensionPropertyControlFactory extFactory;

    /** The UI control to set the <tt>name</tt> property. */
    private Text nameText;

    /** Keep track of the content of the name field to be able to determine whether it has changed. */
    private final String initialName;

    /**
     * The UI control to set the <tt>defaultValueProviderAttribute</tt> property (only for
     * <tt>IEnumLiteralNameAttribute</tt>).
     */
    private Text defaultValueProviderAttributeText;

    /** The UI control to set the <tt>datatype</tt> property. */
    private DatatypeRefControl datatypeControl;

    /** The UI control to set the <tt>multilingual</tt> property. */
    private Checkbox multilingualCheckbox;

    /** The UI control to set the <tt>unique</tt> property. */
    private Checkbox uniqueCheckbox;

    /** The UI control to set the <tt>identifier</tt> property. */
    private Checkbox identifierCheckbox;

    /** The UI control to set the <tt>inherited</tt> property. */
    private Checkbox inheritedCheckbox;

    /** The UI control to set the <tt>usedAsNameInFaktorIpsUi</tt> property. */
    private Checkbox displayNameCheckbox;

    /** The canvas. */
    private Composite workArea;

    private InfoLabel infoLabel;

    /**
     * Flag indicating whether the given <tt>IEnumAttribute</tt> is a
     * <tt>IEnumLiteralNameAttribute</tt> which leads to a variation of this dialog.
     */
    private boolean literalNameAttribute;

    private boolean inherited;

    /**
     * Creates a new <tt>EnumAttributeEditDialog</tt> for the user to edit the given
     * <tt>IEnumAttribute</tt> with.
     * 
     * @param enumAttribute The <tt>IEnumAttribute</tt> to edit with the dialog.
     * @param parentShell The parent UI shell.
     */
    public EnumAttributeEditDialog(IEnumAttribute enumAttribute, Shell parentShell) {
        super(enumAttribute, parentShell, Messages.EnumAttributeEditDialog_title, true);

        this.enumAttribute = enumAttribute;
        initialName = enumAttribute.getName();
        inherited = enumAttribute.isInherited();
        extFactory = new ExtensionPropertyControlFactory(enumAttribute);
        literalNameAttribute = enumAttribute instanceof IEnumLiteralNameAttribute;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        if (getDialogArea() != null) {
            if (!(changeable)) {
                super.setDataChangeable(changeable);
            } else {
                if (literalNameAttribute) {
                    super.setDataChangeable(changeable);
                } else {
                    rebindContents();
                }
            }
        }
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder tabFolder = (TabFolder)parent;

        TabItem page = new TabItem(tabFolder, SWT.NONE);
        page.setText(Messages.EnumAttributeEditDialog_generalTitle);
        Control generalPage = createGeneralPage(tabFolder);
        page.setControl(generalPage);

        checkEnumTypeAndDatatypeEnumTypeExtensible(enumAttribute);

        return tabFolder;
    }

    /** Creates the general tab. */
    private Control createGeneralPage(TabFolder tabFolder) {
        Composite control = createTabItemComposite(tabFolder, 1, false);
        workArea = getToolkit().createLabelEditColumnComposite(control);

        createInfoLabel(control);

        // Create extension properties on position top.
        extFactory.createControls(workArea, getToolkit(), enumAttribute, IExtensionPropertyDefinition.POSITION_TOP);

        if (literalNameAttribute) {
            createFieldsForLiteralNameAttribute();
        } else {
            createFieldsForNormalAttribute();
            rebindContents();
        }

        // Create extension properties on position bottom.
        extFactory.createControls(workArea, getToolkit(), enumAttribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(getBindingContext());

        // Set the focus into the name field for better usability.
        nameText.setFocus();

        return control;
    }

    private void createInfoLabel(Composite control) {
        getToolkit().createVerticalSpacer(control, 20);
        infoLabel = new InfoLabel(control);
    }

    /** Creates the UI fields for a <tt>IEnumLiteralNameAttribute</tt>. */
    private void createFieldsForLiteralNameAttribute() {
        // Name
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        nameText = getToolkit().createText(workArea);
        getBindingContext().bindContent(nameText, enumAttribute, IIpsElement.PROPERTY_NAME);

        // Default Value Provider Attribute
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDefaultValueProviderAttribute);
        defaultValueProviderAttributeText = getToolkit().createText(workArea);
        getBindingContext().bindContent(defaultValueProviderAttributeText, enumAttribute,
                IEnumLiteralNameAttribute.PROPERTY_DEFAULT_VALUE_PROVIDER_ATTRIBUTE);
    }

    /** Creates the UI fields for a normal <tt>IEnumAttribute</tt>. */
    private void createFieldsForNormalAttribute() {
        // Name
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelName);
        nameText = getToolkit().createText(workArea);

        // Datatype
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDatatype);
        datatypeControl = getToolkit().createDatatypeRefEdit(enumAttribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        filterDatatypes();

        // Multilingual
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelMultilingual);
        multilingualCheckbox = getToolkit().createCheckbox(workArea);
        multilingualCheckbox.setToolTipText(Messages.EnumAttributeEditDialog_hintMultilingual);

        // Identifier
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIdentifier);
        identifierCheckbox = getToolkit().createCheckbox(workArea);

        // Display name
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelDisplayName);
        displayNameCheckbox = getToolkit().createCheckbox(workArea);

        // Unique
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelUnique);
        uniqueCheckbox = getToolkit().createCheckbox(workArea);

        // Inherited
        getToolkit().createFormLabel(workArea, Messages.EnumAttributeEditDialog_labelIsInherited);
        inheritedCheckbox = getToolkit().createCheckbox(workArea);

    }

    /**
     * Searches all the <tt>IEnumType</tt>s that are subclasses of the <tt>IEnumType</tt> the
     * <tt>IEnumAttribute</tt> to edit belongs to. All those sub <tt>IEnumType</tt>s and the
     * <tt>IEnumType</tt> itself will not be available in the data type selection.
     * <p>
     * Also, if the <tt>IEnumType</tt> does contain values all <tt>IEnumType</tt> that do not
     * contain values will be disallowed to select, too.
     */
    private void filterDatatypes() {
        IEnumType enumType = enumAttribute.getEnumType();
        IIpsProject ipsProject = enumType.getIpsProject();
        List<Datatype> disallowedDatatypes = new ArrayList<Datatype>();

        // Disallow parent EnumType.
        disallowedDatatypes.add(new EnumTypeDatatypeAdapter(enumType, null));

        // Go once over all EnumTypeDatatypeAdapters.
        for (EnumDatatype currentEnumDatatype : ipsProject.findEnumDatatypes()) {
            if (currentEnumDatatype instanceof EnumTypeDatatypeAdapter) {
                EnumTypeDatatypeAdapter adapter = (EnumTypeDatatypeAdapter)currentEnumDatatype;
                IEnumType enumTypeOfDatatype = adapter.getEnumType();

                // Continue if it is the parent EnumType that we have already disallowed.
                if (enumTypeOfDatatype.equals(enumType)) {
                    continue;
                }

                // Disallow if it is not containing values while the parent EnumType does.
                if (enumType.isInextensibleEnum()) {
                    if (enumTypeOfDatatype.isExtensible()) {
                        disallowedDatatypes.add(adapter);
                        continue;
                    }
                }

                // Disallow if it is a subtype of the parent EnumType.
                if (enumTypeOfDatatype.isSubEnumTypeOf(enumType, ipsProject)) {
                    disallowedDatatypes.add(adapter);
                }
            }
        }

        datatypeControl.setDisallowedDatatypes(disallowedDatatypes);
    }

    /**
     * Binds the contents of the fields to the contents of the super enumeration attribute if the
     * <tt>IEnumAttribute</tt> to be edited is marked as being inherited. Otherwise the contens are
     * bound to the respective properties of the attribute.
     * <p>
     * Also handles the enabled states of the fields.
     * <p>
     * Only applicable if the enumeration attribute isn't marked as literal name.
     */
    private void rebindContents() {
        getBindingContext().clear();

        getBindingContext().bindContent(nameText, enumAttribute, IIpsElement.PROPERTY_NAME);
        if (enumAttribute.isInherited()) {
            obtainContentsFromSuperEnumAttribute();
        } else {
            getBindingContext().bindContent(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_DATATYPE);
            getBindingContext().bindContent(identifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_IDENTIFIER);
            getBindingContext().bindContent(displayNameCheckbox, enumAttribute,
                    IEnumAttribute.PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI);
            getBindingContext().bindContent(uniqueCheckbox, enumAttribute, IEnumAttribute.PROPERTY_UNIQUE);
        }
        getBindingContext().bindContent(multilingualCheckbox, enumAttribute, IEnumAttribute.PROPERTY_MULTILINGUAL);
        getBindingContext().bindContent(inheritedCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED);

        bindEnabledStates();
    }

    private void obtainContentsFromSuperEnumAttribute() {
        try {
            IIpsProject ipsProject = enumAttribute.getIpsProject();
            Datatype datatype = enumAttribute.findDatatype(ipsProject);
            String datatypeName = (datatype == null) ? "" : datatype.getName(); //$NON-NLS-1$
            datatypeControl.setText(datatypeName);
            uniqueCheckbox.setChecked(enumAttribute.findIsUnique(ipsProject));
            identifierCheckbox.setChecked(enumAttribute.findIsIdentifier(ipsProject));
            displayNameCheckbox.setChecked(enumAttribute.findIsUsedAsNameInFaktorIpsUi(ipsProject));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private void bindEnabledStates() {
        getBindingContext().bindEnabled(datatypeControl, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        getBindingContext().bindEnabled(uniqueCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        getBindingContext().bindEnabled(identifierCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        getBindingContext().bindEnabled(displayNameCheckbox, enumAttribute, IEnumAttribute.PROPERTY_INHERITED, false);
        getBindingContext().bindEnabled(multilingualCheckbox, enumAttribute,
                IEnumAttribute.PROPERTY_MULTILINGUAL_SUPPORTED, true);
    }

    @Override
    protected void contentsChangedInternal(final ContentChangeEvent event) {
        if (event.getPart() instanceof IEnumAttribute) {
            IEnumAttribute changedPart = (IEnumAttribute)event.getPart();
            if (changedPart == null) {
                return;
            }

            if (changedPart.equals(enumAttribute)) {
                if (inherited != enumAttribute.isInherited()) {
                    inherited = !(inherited);
                    inheritedChanged();
                }
            }
            if (!literalNameAttribute) {
                updateInfoText(changedPart);
            }
        }
    }

    private void updateInfoText(IEnumAttribute enumAttribute) {
        resetInfoText();
        checkValueTypeMismatch(enumAttribute);
        checkEnumTypeAndDatatypeEnumTypeExtensible(enumAttribute);
    }

    private void checkValueTypeMismatch(IEnumAttribute enumAttribute) {
        IEnumType enumType = enumAttribute.getEnumType();
        String defaultlanguage = enumType.getIpsProject().getReadOnlyProperties().getDefaultLanguage().getLocale()
                .getLanguage();
        ValueTypeMismatch typeMismatch = enumType.checkValueTypeMismatch(enumAttribute);
        if (ValueTypeMismatch.STRING_TO_INTERNATIONAL_STRING.equals(typeMismatch)) {
            setInfoText(NLS.bind(Messages.EnumAttributeEditDialog_mismatchMultilingual, defaultlanguage));
        } else if (ValueTypeMismatch.INTERNATIONAL_STRING_TO_STRING.equals(typeMismatch)) {
            setInfoText(NLS.bind(Messages.EnumAttributeEditDialog_mismatchNoMultilingual, defaultlanguage));
        }
    }

    private void checkEnumTypeAndDatatypeEnumTypeExtensible(IEnumAttribute enumAttribute) {
        IEnumType enumType = enumAttribute.getEnumType();
        Datatype ipsDatatype = enumType.getIpsProject().findDatatype(enumAttribute.getDatatype());
        if (enumType.isExtensible() && DatatypeUtil.isExtensibleEnumType(ipsDatatype)) {
            setInfoText(NLS.bind(Messages.EnumAttributeEditDialog_EnumAttribute_EnumDatatypeExtensibleShowHint,
                    StringUtil.unqualifiedName(ipsDatatype.getQualifiedName())));
        }
    }

    private void setInfoText(String text) {
        infoLabel.setInfoText(text);
    }

    private void resetInfoText() {
        infoLabel.setInfoText(null);
    }

    private void inheritedChanged() {
        if (!(literalNameAttribute)) {
            rebindContents();
            getBindingContext().updateUI();
        }
    }

    @Override
    protected void okPressed() {
        String newName = enumAttribute.getName();
        if (!(newName.equals(initialName))) {
            if (IpsPlugin.getDefault().getIpsPreferences().isRefactoringModeDirect()) {
                applyRenameRefactoring(newName);
            }
        }
        if (!literalNameAttribute) {
            IEnumType enumType = enumAttribute.getEnumType();
            enumType.fixEnumAttributeValues(enumAttribute);
        }
        super.okPressed();
    }

    private void applyRenameRefactoring(String newName) {
        // First, reset the initial name as otherwise the error 'names must not equal' will occur
        enumAttribute.setName(initialName);

        IIpsRefactoring ipsRenameRefactoring = IpsPlugin.getIpsRefactoringFactory().createRenameRefactoring(
                enumAttribute, newName, null, false);
        IpsRefactoringOperation refactoringOperation = new IpsRefactoringOperation(ipsRenameRefactoring, getShell());
        refactoringOperation.runDirectExecution();
    }
}
