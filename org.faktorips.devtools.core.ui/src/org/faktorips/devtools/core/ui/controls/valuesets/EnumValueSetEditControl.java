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

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

/**
 * A control to define an enum value set by specifying the values in it.
 * <p>
 * It consists of a table control and of buttons to add and remove values and to change the order of
 * the values in the set. The control modifies the EnumValueSet and makes no temporary copy of it.
 * To implement undo operation, the EnumValueSet must be stored localy before calling
 * EnumValueSetEditControl.
 */
public class EnumValueSetEditControl extends EditTableControl implements IValueSetEditControl {

    // The value set being edited
    private IEnumValueSet valueSet;
    private final ValueDatatype valueDatatype;
    private final IIpsProject ipsProject;

    /**
     * Constructs a EnumValueSetEditControl and handles the type of the value set that is, if the
     * value set is of the wrong type, a new EnumValueEnumSet is created
     */
    public EnumValueSetEditControl(Composite parent, ValueDatatype valueDatatype, IIpsProject ipsProject) {
        super(parent, SWT.NONE);
        this.valueDatatype = valueDatatype;
        this.ipsProject = ipsProject;
    }

    @Override
    public void initialize(Object modelObject, String label) {
        if (label == null) {
            label = Messages.EnumValueSetEditControl_titleValues;
        }
        super.initialize(modelObject, label);
        GridLayout layout = (GridLayout)getLayout();
        layout.marginHeight = 10;

        initCellEditorsAndConfigureTableViewer();
        new MessageService(getTableViewer());
    }

    @Override
    protected void initModelObject(Object modelObject) {
        valueSet = (EnumValueSet)modelObject;
    }

    public IEnumValueSet getEnumValueSet() {
        return valueSet;
    }

    public void setEnumValueSet(IEnumValueSet valueSet) {
        this.valueSet = valueSet;
        getTableViewer().setInput(valueSet);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.ENUM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        if (valueDatatype == null) {
            return false;
        }
        return valueSet.isEnum() && !valueDatatype.isEnum();
    }

    @Override
    public IValueSet getValueSet() {
        return getEnumValueSet();
    }

    @Override
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        setEnumValueSet((IEnumValueSet)newSet);
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ContentProvider();
    }

    @Override
    protected ILabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }

    @Override
    protected void createTableColumns(Table table) {
        new TableColumn(table, SWT.NONE).setResizable(false);
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);
        new TableColumn(table, ctrlFactory.getDefaultAlignment()).setResizable(false);
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[] { Messages.EnumValueSetEditControl_colName_1, Messages.EnumValueSetEditControl_colName_2 };
    }

    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        layouter.addColumnData(new ColumnPixelData(15, false)); // message image
        layouter.addColumnData(new ColumnWeightData(95, true));
    }

    @Override
    protected CellEditor[] createCellEditors() {
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);
        CellEditor[] editors = new CellEditor[2];
        editors[0] = null; // no editor for the message image column
        editors[1] = ctrlFactory.createTableCellEditor(getUiToolkit(), valueDatatype, valueSet, getTableViewer(), 1,
                ipsProject);
        return editors;
    }

    @Override
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    @Override
    public Object addElement() {
        String newValue = ""; //$NON-NLS-1$
        valueSet.addValue(newValue);
        return newValue;
    }

    @Override
    public void removeElement(int index) {
        valueSet.removeValue(index);
        getTableViewer().refresh();
    }

    @Override
    protected void swapElements(int index1, int index2) {
        String name1 = valueSet.getValue(index1);
        String name2 = valueSet.getValue(index2);
        valueSet.setValue(index1, name2);
        valueSet.setValue(index2, name1);
    }

    private MessageList validate(Object element) throws CoreException {
        IndexValueWrapper wrapper = (IndexValueWrapper)element;
        if (valueSet == null) {
            return new MessageList();
        }
        return valueSet.validateValue(wrapper.index, valueSet.getIpsProject());
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != 0) {
                return null;
            }
            try {
                MessageList list = validate(element);
                return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                        false);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return ""; //$NON-NLS-1$
            }
            String value = IpsUIPlugin.getDefault().getDatatypeFormatter()
                    .formatValue(valueDatatype, element.toString());
            return value;
        }
    }

    private class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            int len = valueSet.getValues().length;
            IndexValueWrapper[] wrappers = new IndexValueWrapper[len];
            for (int i = 0; i < len; i++) {
                wrappers[i] = new IndexValueWrapper(i);
            }
            return wrappers;
        }

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }

    }

    private class CellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            return isDataChangeable();
        }

        @Override
        public Object getValue(Object element, String property) {
            IndexValueWrapper wrapper = (IndexValueWrapper)element;
            return wrapper.getValueName();
        }

        @Override
        public void modify(Object element, String property, Object value) {
            if (element == null) {
                return;
            }
            if (element instanceof Item) {
                element = ((Item)element).getData();
            }
            IndexValueWrapper wrapper = (IndexValueWrapper)element;
            wrapper.setValueName((String)value);
            getTableViewer().update(element, null);
        }
    }

    private class IndexValueWrapper {
        private int index;

        IndexValueWrapper(int index) {
            this.index = index;
        }

        String getValueName() {
            String name = valueSet.getValue(index);
            return name;
        }

        void setValueName(String newName) {
            valueSet.setValue(index, newName);
        }

        @Override
        public String toString() {
            return getValueName();
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IndexValueWrapper)) {
                return false;
            }
            return index == ((IndexValueWrapper)o).index;
        }
    }

    private class MessageService extends TableMessageHoverService {

        public MessageService(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected MessageList getMessagesFor(Object element) throws CoreException {
            return validate(element);
        }

    }

}
