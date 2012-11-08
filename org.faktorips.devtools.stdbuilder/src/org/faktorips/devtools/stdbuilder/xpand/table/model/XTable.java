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

package org.faktorips.devtools.stdbuilder.xpand.table.model;

import java.util.LinkedHashSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.internal.Table;

/**
 * This is the generator model node representing a {@link ITableStructure}.
 * <p>
 * Note: At the moment only the table is not generated by the new Xpand builder. This class is only
 * used to get the correct interface and implementation name.
 * 
 * @author dirmeier
 */
public class XTable extends XClass {

    public XTable(ITableStructure policyCmptType, GeneratorModelContext context, ModelService modelService) {
        super(policyCmptType, context, modelService);
    }

    @Override
    public boolean isValidForCodeGeneration() {
        try {
            return getIpsObjectPartContainer().isValid(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected String getBaseSuperclassName() {
        return addImport(Table.class);
    }

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        return new LinkedHashSet<String>();
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        LinkedHashSet<String> interfaces = new LinkedHashSet<String>();
        interfaces.add(addImport(ITable.class));
        return interfaces;
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        return new LinkedHashSet<String>();
    }
}