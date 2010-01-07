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

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * Page to preview the changes to the names of copied products and to switch between a copy or a
 * reference.
 * 
 * @author Thorsten Guenther
 */
public class ReferenceAndPreviewPage extends WizardPage {

    // The ID to identify this page.
    public static final String PAGE_ID = "deepCopyWizard.preview"; //$NON-NLS-1$

    // The page where the source for the deep copy operation is defined
    private SourcePage sourcePage;

    // Structure to represent
    private IProductCmptTreeStructure structure;

    // The viewer to display the products to copy
    private TreeViewer tree;

    // The type of the wizard displaying this page. Used to show different titles for different
    // types.
    private int type;

    // Label shows the current working date
    private Label workingDateLabel;

    /*
     * @param type The type of the wizard displaying this page.
     * 
     * @return The title for this page - which depends on the given type.
     */
    private static String getTitle(int type) {
        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            return Messages.ReferenceAndPreviewPage_title;
        } else {
            return NLS.bind(Messages.ReferenceAndPreviewPage_titleNewVersion, IpsPlugin.getDefault()
                    .getIpsPreferences().getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        }
    }

    /**
     * Create a new page to show the previously selected products with new names and allow the user
     * to choose between copy and reference, select the target package, search- and replace-pattern.
     * 
     * @param deepCopyWizard
     * 
     * @param structure The product component structure to copy.
     * @param sourcePage The page to get the objects selected for copy, the target package and the
     *            search and replace patterns.
     * @param type The type used to create the <code>DeepCopyWizard</code>.
     * 
     * @throws IllegalArgumentException if the given type is neither
     *             DeepCopyWizard.TYPE_COPY_PRODUCT nor DeepCopyWizard.TYPE_NEW_VERSION.
     */
    protected ReferenceAndPreviewPage(ProductCmptTreeStructure structure, SourcePage sourcePage, int type) {
        super(PAGE_ID, getTitle(type), null);

        if (type != DeepCopyWizard.TYPE_COPY_PRODUCT && type != DeepCopyWizard.TYPE_NEW_VERSION) {
            throw new IllegalArgumentException("The given type is neither TYPE_COPY_PRODUCT nor TYPE_NEW_VERSION."); //$NON-NLS-1$
        }

        this.type = type;

        this.sourcePage = sourcePage;
        this.structure = structure;
        setTitle(getTitle(type));
        setDescription(Messages.ReferenceAndPreviewPage_description);
        setPageComplete(true);
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {

        if (structure == null) {
            Label errormsg = new Label(parent, SWT.WRAP);
            GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, false);
            errormsg.setLayoutData(layoutData);
            errormsg.setText(Messages.ReferenceAndPreviewPage_msgCircleDetected);
            setControl(errormsg);
            return;
        }

        UIToolkit toolkit = new UIToolkit(null);

        Composite root = toolkit.createComposite(parent);
        root.setLayout(new GridLayout(1, false));
        root.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        setControl(root);

        Composite inputRoot = toolkit.createLabelEditColumnComposite(root);

        toolkit.createFormLabel(inputRoot, Messages.ReferenceAndPreviewPage_labelValidFrom);
        workingDateLabel = toolkit.createFormLabel(inputRoot, ""); //$NON-NLS-1$
        updateWorkingDateLabel();

        tree = new TreeViewer(root);
        tree.setUseHashlookup(true);
        tree.setLabelProvider(new LabelProvider());
        tree.setContentProvider(new ContentProvider());
        tree.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void updateWorkingDateLabel() {
        workingDateLabel.setText(getDeepCopyWizard().getFormattedStructureDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        structure = sourcePage.getStructure();
        if (visible) {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
            pmd.setOpenOnRun(true);
            try {
                getWizard().getContainer().run(false, false, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        updateWorkingDateLabel();

                        if (monitor == null) {
                            monitor = new NullProgressMonitor();
                        }

                        monitor.beginTask(Messages.ReferenceAndPreviewPage_msgValidateCopy, 6);
                        ((ContentProvider)tree.getContentProvider()).setCheckedNodes(sourcePage.getCheckedNodes());
                        monitor.worked(1);
                        tree.setInput(structure);
                        monitor.worked(1);
                        tree.expandAll();
                        monitor.worked(1);
                        monitor.worked(1);
                        monitor.worked(1);
                        setDescription(Messages.ReferenceAndPreviewPage_labelTargetPackage + ": "
                                + getDeepCopyWizard().getDeepCopyPreview().getTargetPackage().getName());
                        monitor.worked(1);
                    }
                });
            } catch (InvocationTargetException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            } catch (InterruptedException e) {
                IpsPlugin.logAndShowErrorDialog(e);
            }
        }
    }

    /**
     * Constructs the new name. If at least one of search pattern and replace text is empty, the new
     * name is the old name.
     */
    public String getNewName(IIpsPackageFragment targetPackage, IIpsObject correspondingIpsObject) {
        return getNewName(targetPackage, correspondingIpsObject, 0);
    }

    private String getNewName(IIpsPackageFragment targetPackage,
            IIpsObject correspondingIpsObject,
            int uniqueCopyOfCounter) {
        String oldName = correspondingIpsObject.getName();
        String newName = oldName;
        IProductCmptNamingStrategy namingStrategy = sourcePage.getNamingStrategy();
        String kindId = null;

        if (namingStrategy != null && namingStrategy.supportsVersionId()) {
            MessageList list = namingStrategy.validate(newName);
            if (!list.containsErrorMsg()) {
                kindId = namingStrategy.getKindId(newName);
                newName = namingStrategy.getProductCmptName(namingStrategy.getKindId(newName), sourcePage.getVersion());
            } else {
                // could't determine kind id, thus add copy of in front of the name
                // to get an unique new name
                if (targetPackage != null) {
                    newName = org.faktorips.devtools.core.util.StringUtils.computeCopyOfName(uniqueCopyOfCounter,
                            newName);
                }
            }
        }

        if (type == DeepCopyWizard.TYPE_COPY_PRODUCT) {
            // the copy product feature supports pattern replace
            String searchPattern = sourcePage.getSearchPattern();
            String replaceText = sourcePage.getReplaceText();
            if (!replaceText.equals("") && !searchPattern.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                newName = newName.replaceAll(searchPattern, replaceText);
            }
        }

        if (namingStrategy == null && oldName.equals(newName)) {
            // programming error, should be assert before this page will be displayed
            throw new RuntimeException(
                    "No naming strategy exists, therefore the new product components couldn't be copied with the same name in the same directory!"); //$NON-NLS-1$
        }

        // if no kind is was found check and avoid duplicate names
        // because a copyOf was added in front of the new name
        if (kindId == null && targetPackage != null) {
            IIpsSrcFile ipsSrcFile = targetPackage.getIpsSrcFile(correspondingIpsObject.getIpsObjectType().getFileName(
                    newName));
            if (ipsSrcFile.exists()) {
                return getNewName(targetPackage, correspondingIpsObject, ++uniqueCopyOfCounter);
            }
        }

        return newName;
    }

    /**
     * Returns whether an error message exists for the given object or not.
     */
    private boolean isInError(IProductCmptStructureReference object) {
        return getErrorElements().containsKey(object);
    }

    /**
     * Returns the error message for the given object or <code>null</code>, if no message exists.
     */
    private String getErrorMessage(IProductCmptStructureReference object) {
        return getErrorElements().get(object);
    }

    // #################################################################################

    /**
     * Provides the new names (for selected nodes) and icons showing if a reference is created (for
     * deselected nodes).
     * 
     * @author Thorsten Guenther
     */
    private class LabelProvider extends StyledCellLabelProvider {

        private ResourceManager resourceManager;

        public LabelProvider() {
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }

        private IIpsElement getWrapped(IProductCmptStructureReference in) {
            if (in instanceof IProductCmptReference) {
                return ((IProductCmptReference)in).getProductCmpt();
            } else if (in instanceof IProductCmptTypeRelationReference) {
                return ((IProductCmptTypeRelationReference)in).getRelation();
            } else if (in instanceof IProductCmptStructureTblUsageReference) {
                return ((IProductCmptStructureTblUsageReference)in).getTableContentUsage();
            }
            return null;
        }

        @Override
        public void update(ViewerCell cell) {
            Object element = cell.getElement();
            updateCell(cell, element);
            super.update(cell);
        }

        private void updateCell(ViewerCell cell, Object item) {
            String suffix = getSuffixFor(item);
            StyleRange styledPath = new StyleRange();
            String name = getText(item);
            styledPath.start = name.length();
            styledPath.length = suffix.length();
            styledPath.foreground = getCurrentDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
            styledPath.fontStyle = SWT.NORMAL;
            cell.setText(name + suffix);
            cell.setStyleRanges(new StyleRange[] { styledPath });
            cell.setImage(getImage(item));
        }

        private String getSuffixFor(Object item) {
            if (item instanceof IProductCmptReference) {
                String packageName = getDeepCopyWizard().getDeepCopyPreview().getPackageName(
                        (IProductCmptReference)item);
                return " - " + packageName;
            } else if (item instanceof IProductCmptStructureTblUsageReference) {
                String packageName = getDeepCopyWizard().getDeepCopyPreview().getPackageName(
                        (IProductCmptStructureTblUsageReference)item);
                return " - " + packageName;
            }
            return "";
        }

        private Display getCurrentDisplay() {
            return Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        }

        public Image getImage(Object element) {
            if (element instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference structureReference = (IProductCmptStructureReference)element;
                if (isInError(structureReference)) {
                    return IpsUIPlugin.getImageHandling().getSharedImage("structureReference", true);
                }
                IIpsElement wrapped = getWrapped(structureReference);
                if (wrapped instanceof IProductCmpt) {
                    if (getDeepCopyWizard().getDeepCopyPreview().isLinked(element)) {
                        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                                "LinkProductCmpt.gif");
                        return (Image)resourceManager.get(imageDescriptor);
                    }
                } else if (wrapped instanceof ITableContentUsage) {
                    if (getDeepCopyWizard().getDeepCopyPreview().isLinked(element)) {
                        ImageDescriptor imageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
                                "LinkTableContents.gif");
                        return (Image)resourceManager.get(imageDescriptor);
                    }
                }
                Image image = IpsUIPlugin.getImageHandling().getImage(wrapped);
                return image;
            } else {
                return null;
            }
        }

        public String getText(Object element) {
            if (element instanceof IProductCmptStructureReference) {
                IProductCmptStructureReference structureReference = (IProductCmptStructureReference)element;
                Object wrapped = getWrapped(structureReference);
                if (wrapped instanceof IProductCmpt) {
                    String name = ((IProductCmpt)wrapped).getName();
                    if (!getDeepCopyWizard().getDeepCopyPreview().isLinked(structureReference)) {
                        name = getDeepCopyWizard().getDeepCopyPreview().getOldObject2newNameMap().get(
                                structureReference);
                        if (name == null) {
                            name = getNewName(null, (IIpsObject)wrapped);
                        }
                    }
                    if (isInError(structureReference)) {
                        name = name + Messages.ReferenceAndPreviewPage_errorLabelInsert
                                + getErrorMessage(structureReference);
                    }
                    return name;
                } else if (wrapped instanceof ITableContentUsage) {
                    String name = StringUtil.unqualifiedName(((ITableContentUsage)wrapped).getTableContentName());
                    if (!getDeepCopyWizard().getDeepCopyPreview().isLinked(structureReference)) {
                        name = getDeepCopyWizard().getDeepCopyPreview().getOldObject2newNameMap().get(
                                structureReference);
                        if (name == null) {
                            try {
                                ITableContents tableContents = ((ITableContentUsage)wrapped)
                                        .findTableContents(getDeepCopyWizard().getIpsProject());
                                name = getNewName(null, tableContents);
                            } catch (CoreException e) {
                                // should be displayed as validation error before
                                IpsPlugin.log(e);
                            }
                        }
                    }
                    if (isInError(structureReference)) {
                        name = name + Messages.ReferenceAndPreviewPage_errorLabelInsert
                                + getErrorMessage(structureReference);
                    }
                    return name;
                }
                return ((IIpsObjectPartContainer)wrapped).getName();
            } else {
                return element.toString();
            }
        }

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
            resourceManager.dispose();
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return true;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }
    }

    /**
     * Does only show the nodes which where selected on the source page. As input, an array of all
     * the selected nodes of the source page is expected.
     * 
     * @author Thorsten Guenther
     */
    private class ContentProvider extends DeepCopyContentProvider {

        private Set<IProductCmptStructureReference> checkedNodes;

        public ContentProvider() {
            super(true);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            IProductCmptStructureReference[] children = (IProductCmptStructureReference[])super
                    .getChildren(parentElement);
            List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();
            for (int i = 0; i < children.length; i++) {
                if (isChecked(children[i])
                        || (!(children[i] instanceof IProductCmptReference) && !isUncheckedSubtree(new IProductCmptStructureReference[] { children[i] }))) {
                    result.add(children[i]);
                }
            }
            return result.toArray(new IProductCmptStructureReference[result.size()]);
        }

        private boolean isUncheckedSubtree(IProductCmptStructureReference[] children) {
            boolean unchecked = true;
            for (int i = 0; i < children.length && unchecked; i++) {
                if (children[i] instanceof IProductCmptReference) {
                    if (isChecked(children[i])) {
                        return false;
                    }
                } else if (children[i] instanceof IProductCmptTypeRelationReference) {
                    unchecked = unchecked && isUncheckedSubtree(structure.getChildProductCmptReferences(children[i]));
                }
            }
            return unchecked;
        }

        @Override
        public Object getParent(Object element) {
            Object parent = super.getParent(element);

            if (parent == null) {
                return null;
            }

            while (!isChecked((IProductCmptStructureReference)parent)) {
                parent = super.getParent(parent);
            }
            return parent;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProductCmptTreeStructure) {
                IProductCmptReference node = ((IProductCmptTreeStructure)inputElement).getRoot();
                if (isChecked(node)) {
                    return new Object[] { node };
                }
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
            checkedNodes = null;
        }

        public void setCheckedNodes(IProductCmptStructureReference[] checked) {
            checkedNodes = new HashSet<IProductCmptStructureReference>();
            for (int i = 0; i < checked.length; i++) {
                checkedNodes.add(checked[i]);
            }
        }

        private boolean isChecked(IProductCmptStructureReference node) {
            return checkedNodes != null && checkedNodes.contains(node);
        }
    }

    private DeepCopyWizard getDeepCopyWizard() {
        return (DeepCopyWizard)getWizard();
    }

    public Hashtable<IProductCmptStructureReference, String> getErrorElements() {
        return getDeepCopyWizard().getDeepCopyPreview().getErrorElements();
    }

}
