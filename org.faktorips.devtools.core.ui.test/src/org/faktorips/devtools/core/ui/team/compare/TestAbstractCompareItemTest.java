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

package org.faktorips.devtools.core.ui.team.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.team.compare.tablecontents.TableContentsCompareItem;
import org.faktorips.devtools.core.ui.team.compare.tablecontents.TableContentsCompareItemCreator;
import org.junit.Before;
import org.junit.Test;

public class TestAbstractCompareItemTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new TableContentsCompareItemCreator();
    private ITableContentsGeneration generation;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;
    private IIpsPackageFragmentRoot root;

    private TableContentsCompareItem compareItemRoot;
    private ITableContents table;
    private IRow row1;
    private IRow row2;
    private IRow row3;
    private IRow row4;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject proj = newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        table = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table1");
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        GregorianCalendar calendar = new GregorianCalendar();
        generation = (ITableContentsGeneration)table.newGeneration(calendar);
        row1 = generation.newRow();
        row1.setValue(0, "r1_c1");
        row1.setValue(1, "r1_c2");
        row1.setValue(2, "r1_c3");
        row2 = generation.newRow();
        row3 = generation.newRow();
        row4 = generation.newRow();

        srcFile = table.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        // initialized compareItem
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testGetContents() throws CoreException {
        assertNull(compareItemRoot.getContents());
    }

    @Test
    public void testGetChildren() {
        assertEquals(1, compareItemRoot.getChildren().length);
        TableContentsCompareItem tableItem = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        assertEquals(1, tableItem.getChildren().length);
        TableContentsCompareItem genItem = (TableContentsCompareItem)tableItem.getChildren()[0];
        assertEquals(4, genItem.getChildren().length);
    }

    @Test
    public void testEqualsObject() {
        TableContentsCompareItem secondCompareItemRoot = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile));
        TableContentsCompareItem tableItem = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem = (TableContentsCompareItem)tableItem.getChildren()[0];
        TableContentsCompareItem rowItem1 = (TableContentsCompareItem)genItem.getChildren()[0];
        TableContentsCompareItem rowItem2 = (TableContentsCompareItem)genItem.getChildren()[1];
        assertEquals(compareItemRoot, secondCompareItemRoot);
        assertFalse(compareItemRoot.equals(tableItem));
        // same content, differing row numbers
        assertFalse(rowItem1.equals(rowItem2));
    }

    @Test
    public void testGetImage() {
        assertEquals(IpsUIPlugin.getImageHandling().getImage(compareItemRoot.getIpsElement()),
                compareItemRoot.getImage());
    }

    @Test
    public void testIsRoot() {
        TableContentsCompareItem tableItem = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem = (TableContentsCompareItem)tableItem.getChildren()[0];
        TableContentsCompareItem rowItem = (TableContentsCompareItem)genItem.getChildren()[0];
        assertTrue(compareItemRoot.isRoot());
        assertFalse(tableItem.isRoot());
        assertFalse(genItem.isRoot());
        assertFalse(rowItem.isRoot());

        TableContentsCompareItem compareItem = new TableContentsCompareItem(null, srcFile);
        assertTrue(compareItem.isRoot());
    }

    @Test
    public void testGetParent() {
        TableContentsCompareItem tableItem = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem = (TableContentsCompareItem)tableItem.getChildren()[0];
        TableContentsCompareItem rowItem = (TableContentsCompareItem)genItem.getChildren()[0];
        TableContentsCompareItem rowItem2 = (TableContentsCompareItem)genItem.getChildren()[1];
        TableContentsCompareItem rowItem3 = (TableContentsCompareItem)genItem.getChildren()[2];
        TableContentsCompareItem rowItem4 = (TableContentsCompareItem)genItem.getChildren()[3];
        assertNull(compareItemRoot.getParent());
        assertEquals(compareItemRoot, tableItem.getParent());
        assertEquals(tableItem, genItem.getParent());
        assertEquals(genItem, rowItem.getParent());
        assertEquals(genItem, rowItem2.getParent());
        assertEquals(genItem, rowItem3.getParent());
        assertEquals(genItem, rowItem4.getParent());

        TableContentsCompareItem compareItem = new TableContentsCompareItem(null, srcFile);
        assertNull(compareItem.getParent());
    }

    @Test
    public void testGetIpsElement() {
        TableContentsCompareItem tableItem = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem = (TableContentsCompareItem)tableItem.getChildren()[0];
        TableContentsCompareItem rowItem = (TableContentsCompareItem)genItem.getChildren()[0];
        TableContentsCompareItem rowItem2 = (TableContentsCompareItem)genItem.getChildren()[1];
        TableContentsCompareItem rowItem3 = (TableContentsCompareItem)genItem.getChildren()[2];
        TableContentsCompareItem rowItem4 = (TableContentsCompareItem)genItem.getChildren()[3];

        assertEquals(srcFile, compareItemRoot.getIpsElement());
        assertEquals(table, tableItem.getIpsElement());

        assertEquals(generation, genItem.getIpsElement());

        assertEquals(row1, rowItem.getIpsElement());
        assertEquals(row2, rowItem2.getIpsElement());
        assertEquals(row3, rowItem3.getIpsElement());
        assertEquals(row4, rowItem4.getIpsElement());
    }

    @Test
    public void testGetDocument() {
        IDocument doc = compareItemRoot.getDocument();
        TableContentsCompareItem tableItem = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem = (TableContentsCompareItem)tableItem.getChildren()[0];
        TableContentsCompareItem rowItem1 = (TableContentsCompareItem)genItem.getChildren()[0];
        TableContentsCompareItem rowItem2 = (TableContentsCompareItem)genItem.getChildren()[1];

        assertEquals(doc, tableItem.getDocument());
        assertEquals(doc, genItem.getDocument());
        assertEquals(doc, rowItem1.getDocument());
        assertEquals(doc, rowItem2.getDocument());
        // test identity
        assertSame(doc, tableItem.getDocument());
        assertSame(doc, genItem.getDocument());
        assertSame(doc, rowItem1.getDocument());
        assertSame(doc, rowItem2.getDocument());
    }

    @Test
    public void testGetRange() {
        // Position range= new Position(23, 42);
        // compareItemRoot.setRange(23, 42);
        // assertEquals(range, compareItemRoot.getRange());
        // test defensive copy
        assertEquals(compareItemRoot.getRange(), compareItemRoot.getRange());
        assertNotSame(compareItemRoot.getRange(), compareItemRoot.getRange());
    }

    @Test
    public void testInit() {
        // create uninitialized compareItem
        TableContentsCompareItem compareItem = new TableContentsCompareItem(null, srcFile);
        assertNull(compareItem.getContentString());
        assertNull(compareItem.getContentStringWithoutWhiteSpace());
        assertNull(compareItem.getName());
        assertNull(compareItem.getDocument());
        compareItem.init();
        assertNotNull(compareItem.getContentString());
        assertNotNull(compareItem.getContentStringWithoutWhiteSpace());
        assertNotNull(compareItem.getName());
        assertNotNull(compareItem.getDocument());
    }

}
