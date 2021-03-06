/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.SingleEventModification;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IProductCmptProperty}.
 */
public class ProductCmptCategory extends AtomicIpsObjectPart implements IProductCmptCategory {

    static final String XML_TAG_NAME = "Category"; //$NON-NLS-1$

    private boolean defaultForFormulaSignatureDefinitions;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    private Position position = Position.LEFT;

    public ProductCmptCategory(IProductCmptType parent, String id) {
        super(parent, id);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    private ProductCmptType getProductCmptTypeImpl() {
        return (ProductCmptType)getProductCmptType();
    }

    @Override
    public boolean findIsContainingProperty(IProductCmptProperty property,
            IProductCmptType contextType,
            IIpsProject ipsProject) throws CoreException {
        // The queried property must be found by the context type
        if (contextType.findProductCmptProperty(property.getPropertyName(), ipsProject) == null) {
            return false;
        }

        String categoryName = ((ProductCmptType)contextType).getCategoryNameFor(property);
        // The name of this category must not be empty and must equal the property's category
        if (StringUtils.isNotEmpty(categoryName) && categoryName.equals(name)) {
            return true;
        }

        if (isDefaultFor(property)) {
            /*
             * If the name of the property's category does not match this category's name, this
             * category still may contain the property if this category is the corresponding default
             * category. In this case, if the property has no category or the property's category
             * cannot be found, the property belongs to this category.
             */
            return StringUtils.isEmpty(categoryName) || !contextType.findHasCategory(categoryName, ipsProject);
        }

        return false;
    }

    @Override
    public boolean isDefaultFor(IProductCmptProperty property) {
        ProductCmptPropertyType productCmptPropertyType = property.getProductCmptPropertyType();
        if (productCmptPropertyType == null) {
            return false;
        }
        return isDefaultFor(productCmptPropertyType);
    }

    @Override
    public boolean isDefaultFor(ProductCmptPropertyType propertyType) {
        boolean isDefault = false;
        switch (propertyType) {
            case POLICY_CMPT_TYPE_ATTRIBUTE:
                isDefault = isDefaultForPolicyCmptTypeAttributes();
                break;
            case PRODUCT_CMPT_TYPE_ATTRIBUTE:
                isDefault = isDefaultForProductCmptTypeAttributes();
                break;
            case VALIDATION_RULE:
                isDefault = isDefaultForValidationRules();
                break;
            case FORMULA_SIGNATURE_DEFINITION:
                isDefault = isDefaultForFormulaSignatureDefinitions();
                break;
            case TABLE_STRUCTURE_USAGE:
                isDefault = isDefaultForTableStructureUsages();
                break;
        }
        return isDefault;
    }

    @Override
    public List<IProductCmptProperty> findProductCmptProperties(IProductCmptType contextType,
            final boolean searchSupertypeHierarchy,
            IIpsProject ipsProject) throws CoreException {

        class CategoryPropertyCollector extends TypeHierarchyVisitor<IProductCmptType> {

            private final List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();

            /**
             * {@link Set} that is used to store all property names of properties that overwrite
             * another property from the supertype hierarchy.
             * <p>
             * When testing whether a given {@link IProductCmptProperty} shall be included in an
             * {@link IProductCmptCategory}, it is first checked whether an
             * {@link IProductCmptProperty} with the same property name is contained within this
             * set. In this case, the {@link IProductCmptProperty} has been overwritten by a subtype
             * which means that the supertype {@link IProductCmptProperty} is not to be added to the
             * {@link IProductCmptCategory}.
             */
            private final Set<String> overwritingProperties = new HashSet<String>();

            private CategoryPropertyCollector(IIpsProject ipsProject) {
                super(ipsProject);
            }

            @Override
            protected boolean visit(IProductCmptType currentType) {
                try {
                    for (IProductCmptProperty property : currentType.findProductCmptProperties(false,
                            getIpsProject())) {
                        /*
                         * First, check whether the property has been overwritten by a subtype - in
                         * this case we do not add the property to the category.
                         */
                        if (overwritingProperties.contains(property.getPropertyName())) {
                            continue;
                        }

                        /*
                         * Memorize the property if it is overwriting another property from the
                         * supertype hierarchy.
                         */
                        if (isOverwriteProperty(property)) {
                            overwritingProperties.add(property.getPropertyName());
                        }

                        /*
                         * Now, check if the property is visible. If not, the property will not be
                         * added to the category. Note that it is still important to check if it is
                         * overwritten, first, so that super attributes that are not hidden, will
                         * not be displayed.
                         */
                        if (!isVisible(property)) {
                            continue;
                        }

                        if (findIsContainingProperty(property, currentType, getIpsProject())
                                && !properties.contains(property)) {
                            properties.add(property);
                        }
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }

                return searchSupertypeHierarchy;
            }

            /**
             * Returns whether the given {@link IProductCmptProperty} overwrites another
             * {@link IProductCmptProperty} from the supertype hierarchy.
             */
            private boolean isOverwriteProperty(IProductCmptProperty property) {
                if (property instanceof IAttribute) {
                    return ((IAttribute)property).isOverwrite();
                } else if (property instanceof IProductCmptTypeMethod) {
                    return ((IProductCmptTypeMethod)property).isOverloadsFormula();
                }
                return false;
            }

            /**
             * Returns true if the given {@link IProductCmptProperty} is visible, false otherwise.
             */
            private boolean isVisible(IProductCmptProperty property) {
                if (property instanceof IProductCmptTypeAttribute) {
                    return ((IProductCmptTypeAttribute)property).isVisible();
                }
                return true;
            }

        }

        CategoryPropertyCollector collector = new CategoryPropertyCollector(ipsProject);
        collector.start(contextType);

        Collections.sort(collector.properties, new ProductCmptPropertyComparator(contextType));

        return collector.properties;
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name, PROPERTY_NAME);
    }

    @Override
    public boolean isDefaultForFormulaSignatureDefinitions() {
        return defaultForFormulaSignatureDefinitions;
    }

    @Override
    public void setDefaultForFormulaSignatureDefinitions(boolean defaultForFormulaSignatureDefinitions) {
        boolean oldValue = this.defaultForFormulaSignatureDefinitions;
        this.defaultForFormulaSignatureDefinitions = defaultForFormulaSignatureDefinitions;
        valueChanged(oldValue, defaultForFormulaSignatureDefinitions,
                PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
    }

    @Override
    public boolean isDefaultForPolicyCmptTypeAttributes() {
        return defaultForPolicyCmptTypeAttributes;
    }

    @Override
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes) {
        boolean oldValue = this.defaultForPolicyCmptTypeAttributes;
        this.defaultForPolicyCmptTypeAttributes = defaultForPolicyCmptTypeAttributes;
        valueChanged(oldValue, defaultForPolicyCmptTypeAttributes, PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public boolean isDefaultForProductCmptTypeAttributes() {
        return defaultForProductCmptTypeAttributes;
    }

    @Override
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes) {
        boolean oldValue = this.defaultForProductCmptTypeAttributes;
        this.defaultForProductCmptTypeAttributes = defaultForProductCmptTypeAttributes;
        valueChanged(oldValue, defaultForProductCmptTypeAttributes, PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public boolean isDefaultForTableStructureUsages() {
        return defaultForTableStructureUsages;
    }

    @Override
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages) {
        boolean oldValue = this.defaultForTableStructureUsages;
        this.defaultForTableStructureUsages = defaultForTableStructureUsages;
        valueChanged(oldValue, defaultForTableStructureUsages, PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
    }

    @Override
    public boolean isDefaultForValidationRules() {
        return defaultForValidationRules;
    }

    @Override
    public void setDefaultForValidationRules(boolean defaultForValidationRules) {
        boolean oldValue = this.defaultForValidationRules;
        this.defaultForValidationRules = defaultForValidationRules;
        valueChanged(oldValue, defaultForValidationRules, PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    @Override
    public void setPosition(Position side) {
        Position oldValue = this.position;
        this.position = side;

        getProductCmptTypeImpl().sortCategoriesAccordingToPosition();

        valueChanged(oldValue, side, PROPERTY_POSITION);
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean isAtLeftPosition() {
        return position == Position.LEFT;
    }

    @Override
    public boolean isAtRightPosition() {
        return position == Position.RIGHT;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (!validateNameIsEmpty(list)) {
            return;
        }
        validateNameAlreadyUsedInTypeHierarchy(list, ipsProject);
        validateDuplicateDefaultsForFormulaSignatureDefinitions(list, ipsProject);
        validateDuplicateDefaultsForPolicyCmptTypeAttributes(list, ipsProject);
        validateDuplicateDefaultsForProductCmptTypeAttributes(list, ipsProject);
        validateDuplicateDefaultsForTableStructureUsages(list, ipsProject);
        validateDuplicateDefaultsForValidationRules(list, ipsProject);
    }

    private boolean validateNameIsEmpty(MessageList list) {
        if (StringUtils.isEmpty(name)) {
            list.newError(MSGCODE_NAME_IS_EMPTY, Messages.ProductCmptCategory_msgNameIsEmpty, this, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private boolean validateNameAlreadyUsedInTypeHierarchy(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (getProductCmptTypeImpl().findIsCategoryNameUsedTwiceInSupertypeHierarchy(name, ipsProject)) {
            String text = NLS.bind(Messages.ProductCmptCategory_msgNameAlreadyUsedInTypeHierarchy, name,
                    getProductCmptType().getName());
            list.newError(MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, text, this, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private void validateDuplicateDefaultsForFormulaSignatureDefinitions(MessageList list, IIpsProject ipsProject) {

        if (!defaultForFormulaSignatureDefinitions) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.FORMULA_SIGNATURE_DEFINITION, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                Messages.ProductCmptCategory_DuplicateDefaultsForFormulaSignatureDefinitions,
                PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
    }

    private void validateDuplicateDefaultsForValidationRules(MessageList list, IIpsProject ipsProject) {

        if (!defaultForValidationRules) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ProductCmptPropertyType.VALIDATION_RULE,
                ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list, MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES,
                Messages.ProductCmptCategory_DuplicateDefaultsForValidationRules,
                PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    private void validateDuplicateDefaultsForTableStructureUsages(MessageList list, IIpsProject ipsProject) {

        if (!defaultForTableStructureUsages) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.TABLE_STRUCTURE_USAGE, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES,
                Messages.ProductCmptCategory_DuplicateDefaultsForTableStructureUsages,
                PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
    }

    private void validateDuplicateDefaultsForPolicyCmptTypeAttributes(MessageList list, IIpsProject ipsProject) {

        if (!defaultForPolicyCmptTypeAttributes) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForPolicyCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
    }

    private void validateDuplicateDefaultsForProductCmptTypeAttributes(MessageList list, IIpsProject ipsProject) {

        if (!defaultForProductCmptTypeAttributes) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(
                ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE, ipsProject);
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationMessageIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForProductCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public int[] moveProductCmptProperties(int[] indexes, boolean up, IProductCmptType contextType)
            throws CoreException {

        if (indexes.length == 0) {
            return new int[0];
        }

        List<IProductCmptProperty> contextProperties = findProductCmptProperties(contextType, false,
                contextType.getIpsProject());
        return ((ProductCmptType)contextType).movePropertyReferences(indexes, contextProperties, up);
    }

    @Override
    public boolean insertProductCmptProperty(final IProductCmptProperty property,
            final IProductCmptProperty targetProperty,
            final boolean above) throws CoreException {

        final IProductCmptType contextType = property.findProductCmptType(property.getIpsProject());
        if (contextType == null) {
            return false;
        }
        // CSOFF: AnonInnerLength
        return getIpsModel()
                .executeModificationsWithSingleEvent(new SingleEventModification<Boolean>(contextType.getIpsSrcFile()) {
                    private boolean result = true;

                    @Override
                    protected boolean execute() throws CoreException {
                        contextType.changeCategoryAndDeferPolicyChange(property, name);
                        List<IProductCmptProperty> properties = findProductCmptProperties(contextType, false,
                                contextType.getIpsProject());
                        int propertyIndex = properties.indexOf(property);
                        int targetPropertyIndex = targetProperty != null ? properties.indexOf(targetProperty)
                                : properties.size() - 1;
                        if (propertyIndex == -1 || targetPropertyIndex == -1) {
                            result = false;
                        } else {
                            insertProductCmptProperty(propertyIndex, targetPropertyIndex, contextType, above);
                        }
                        return true;
                    }

                    @Override
                    protected Boolean getResult() {
                        return result;
                    }
                });
        // CSON: AnonInnerLength
    }

    private void insertProductCmptProperty(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) throws CoreException {

        if (propertyIndex > targetPropertyIndex) {
            moveProductCmptPropertyUp(propertyIndex, targetPropertyIndex, contextType, above);
        } else if (propertyIndex < targetPropertyIndex) {
            moveProductCmptPropertyDown(propertyIndex, targetPropertyIndex, contextType, above);
        }
    }

    /**
     * Moves the {@link IProductCmptProperty} identified by the given index up until it is just
     * above or below the indicated target index.
     */
    private void moveProductCmptPropertyUp(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) throws CoreException {

        int targetIndex = above ? targetPropertyIndex : targetPropertyIndex + 1;
        for (; propertyIndex > targetIndex; propertyIndex--) {
            moveProductCmptProperties(new int[] { propertyIndex }, true, contextType);
        }
    }

    /**
     * Moves the {@link IProductCmptProperty} identified by the given index down until it is just
     * above or below the indicated target index.
     */
    private void moveProductCmptPropertyDown(int propertyIndex,
            int targetPropertyIndex,
            IProductCmptType contextType,
            boolean above) throws CoreException {

        int targetIndex = above ? targetPropertyIndex - 1 : targetPropertyIndex;
        for (; propertyIndex < targetIndex; propertyIndex++) {
            moveProductCmptProperties(new int[] { propertyIndex }, false, contextType);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        defaultForFormulaSignatureDefinitions = Boolean
                .parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS));
        defaultForPolicyCmptTypeAttributes = Boolean
                .parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES));
        defaultForProductCmptTypeAttributes = Boolean
                .parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES));
        defaultForTableStructureUsages = Boolean
                .parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES));
        defaultForValidationRules = Boolean.parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES));
        position = Position.getValueById(element.getAttribute(PROPERTY_POSITION));

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                Boolean.toString(defaultForFormulaSignatureDefinitions));
        element.setAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForPolicyCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForProductCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES,
                Boolean.toString(defaultForTableStructureUsages));
        element.setAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Boolean.toString(defaultForValidationRules));
        element.setAttribute(PROPERTY_POSITION, position.getId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    /**
     * {@link Comparator} that can be used to sort product component properties according to the
     * reference list stored in the {@link IProductCmptType}, with properties belonging to
     * supertypes being sorted towards the beginning of the list by default.
     */
    static class ProductCmptPropertyComparator implements Comparator<IProductCmptProperty> {

        private final IProductCmptType productCmptType;

        ProductCmptPropertyComparator(IProductCmptType productCmptType) {
            this.productCmptType = productCmptType;
        }

        @Override
        public int compare(IProductCmptProperty property1, IProductCmptProperty property2) {
            // First, try to sort properties of the supertype hierarchy to the top
            int subtypeCompare = compareSubtypeRelationship(property1, property2);

            // If the indices are equal, compare the indices of the properties in the reference list
            return subtypeCompare != 0 ? subtypeCompare : comparePropertyIndices(property1, property2);
        }

        /**
         * Compares the provided product component types according to their subtype/supertype
         * relationship.
         * <p>
         * Subtypes are sorted towards the end.
         */
        private int compareSubtypeRelationship(IProductCmptProperty property1, IProductCmptProperty property2) {
            // Search the product component types the properties belong to
            IProductCmptType productCmptType1;
            IProductCmptType productCmptType2;
            try {
                productCmptType1 = property1.findProductCmptType(productCmptType.getIpsProject());
                productCmptType2 = property2.findProductCmptType(productCmptType.getIpsProject());
            } catch (CoreException e) {
                // Consider elements equal if the product component types cannot be found
                IpsPlugin.log(e);
                return 0;
            }

            // Consider elements equal if the product component types cannot be found
            if (productCmptType1 == null || productCmptType2 == null) {
                return 0;
            }

            // Consider elements equal if both properties belong to the same product component type
            if (productCmptType1.equals(productCmptType2)) {
                return 0;
            }

            // Sort supertypes towards the beginning
            if (productCmptType1.isSubtypeOf(productCmptType2, productCmptType.getIpsProject())) {
                return 1;
            } else {
                return -1;
            }
        }

        /**
         * Compares the indices of the given product component properties in the list of property
         * references.
         * <p>
         * Properties whose indices are greater are sorted towards the end.
         */
        private int comparePropertyIndices(IProductCmptProperty property1, IProductCmptProperty property2) {
            IProductCmptType contextType = null;
            try {
                contextType = property1.findProductCmptType(property1.getIpsProject());
            } catch (CoreException e) {
                /*
                 * Consider the properties equal if the product component type containing the
                 * references cannot be found.
                 */
                IpsPlugin.log(e);
                return 0;
            }

            /*
             * Consider the properties equal if the product component type containing the references
             * cannot be found.
             */
            if (contextType == null) {
                return 0;
            }

            int index1 = ((ProductCmptType)contextType).getReferencedPropertyIndex(property1);
            int index2 = ((ProductCmptType)contextType).getReferencedPropertyIndex(property2);

            // If no reference exists for a property, it is sorted towards the end
            if (index1 == -1) {
                index1 = Integer.MAX_VALUE;
            }
            if (index2 == -1) {
                index2 = Integer.MAX_VALUE;
            }

            if (index1 == index2) {
                return 0;
            } else if (index1 < index2) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    /**
     * {@link TypeHierarchyVisitor} that searches for the existence of at least two categories
     * marked as <em>default</em> for a specific {@link ProductCmptPropertyType}.
     */
    private class DuplicateDefaultFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private final ProductCmptPropertyType propertyType;

        private boolean duplicateDefaultFound;

        /**
         * @param propertyType the {@link ProductCmptPropertyType} for which duplicate defaults are
         *            searched
         */
        protected DuplicateDefaultFinder(ProductCmptPropertyType propertyType, IIpsProject ipsProject) {
            super(ipsProject);
            this.propertyType = propertyType;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) {
            for (IProductCmptCategory category : currentType.getCategories()) {
                if (category.isDefaultFor(propertyType) && !name.equals(category.getName())) {
                    duplicateDefaultFound = true;
                    return false;
                }
            }
            return true;
        }

        private void addValidationMessageIfDuplicateFound(MessageList list,
                String code,
                String text,
                String invalidProperty) {

            if (duplicateDefaultFound) {
                list.newWarning(code, text, ProductCmptCategory.this, invalidProperty);
            }
        }

    }

}
