/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectEditorSettingsTest extends AbstractIpsPluginTest {

    private IpsObjectEditorSettings settings;
    private IIpsProject project;
    private IIpsSrcFile srcFile;
    
    protected void setUp() throws Exception {
        super.setUp();
        settings = new IpsObjectEditorSettings();
        project = newIpsProject();
        srcFile = newPolicyCmptType(project, "motor.Policy").getIpsSrcFile();
    }
    
    /**
     * Test method for {@link org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings#put(org.faktorips.devtools.core.model.IIpsSrcFile, java.lang.String, java.lang.String)}.
     * @throws CoreException 
     */
    public void testPut() throws CoreException {
        settings.put(srcFile, "KEY1", "value1");
        assertEquals("value1", settings.get(srcFile, "KEY1"));
        
        settings.put(srcFile, "KEY1", "value1a");
        assertEquals("value1a", settings.get(srcFile, "KEY1"));
        
        settings.put(srcFile, "KEY2", "value2");
        assertEquals("value1a", settings.get(srcFile, "KEY1"));
        assertEquals("value2", settings.get(srcFile, "KEY2"));
        
        IIpsSrcFile srcFile2 = newPolicyCmptType(project, "home.Policy").getIpsSrcFile();
        settings.put(srcFile2, "KEY1", "value3");
        assertEquals("value3", settings.get(srcFile2, "KEY1"));
        assertEquals("value1a", settings.get(srcFile, "KEY1"));
        assertEquals("value2", settings.get(srcFile, "KEY2"));

        settings.put(srcFile, "KEY1", null);
        assertNull(settings.get(srcFile, "KEY1"));
        
        try {
            settings.put(srcFile, null, "");
            fail();
        } catch (NullPointerException e) {
        }
        
        try {
            settings.put(srcFile, "KEY CONTAINGING A BLANK", "");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings#get(org.faktorips.devtools.core.model.IIpsSrcFile, java.lang.String)}.
     */
    public void testGet() {
        assertNull(settings.get(srcFile, "KEY1"));
        settings.put(srcFile, "KEY1", "value1");
        assertEquals("value1", settings.get(srcFile, "KEY1"));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings#get(org.faktorips.devtools.core.model.IIpsSrcFile, java.lang.String)}.
     */
    public void testGetBoolean() {
        assertFalse(settings.getBoolean(srcFile, "KEY1"));
        settings.put(srcFile, "KEY1", true);
        assertTrue(settings.getBoolean(srcFile, "KEY1"));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings#remove(org.faktorips.devtools.core.model.IIpsSrcFile)}.
     */
    public void testRemoveIIpsSrcFile() {
        settings.remove(srcFile);

        settings.put(srcFile, "KEY1", "value1");
        settings.put(srcFile, "KEY2", "value2");
        settings.remove(srcFile);
        
        assertNull(settings.get(srcFile, "KEY1"));
        assertNull(settings.get(srcFile, "KEY2"));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings#remove(org.faktorips.devtools.core.model.IIpsSrcFile, java.lang.String)}.
     */
    public void testRemoveIIpsSrcFileString() {
        settings.remove(srcFile, "KEY1");

        settings.put(srcFile, "KEY1", "value1");
        settings.put(srcFile, "KEY2", "value2");
        settings.remove(srcFile, "KEY2");
        
        assertEquals("value1", settings.get(srcFile, "KEY1"));
        assertNull(settings.get(srcFile, "KEY2"));
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.ui.editors.IpsObjectEditorSettings#save(java.io.File)}.
     * @throws CoreException 
     * @throws IOException 
     */
    public void testLoadSave() throws CoreException, IOException {
        settings.put(srcFile, "KEY1", "value1");
        settings.put(srcFile, "KEY2", "value2");
        
        IIpsSrcFile srcFile2 = newPolicyCmptType(project, "home.Policy").getIpsSrcFile();
        settings.put(srcFile2, "KEY1", "value1");
        settings.remove(srcFile2);
        // not key/values for this file
        
        IIpsSrcFile srcFile3 = newPolicyCmptType(project, "Product 2005").getIpsSrcFile();
        settings.put(srcFile3, "KEY1", "value3");
        settings.put(srcFile3, "KEY2", "value4");
        
        IIpsSrcFile srcFile4 = newPolicyCmptType(project, "Product 2006").getIpsSrcFile();
        settings.put(srcFile4, "KEY1", "value5");
        settings.put(srcFile4, "KEY2", "value6");

        File file = File.createTempFile("IpsEditorSettingsTest", ".txt");
        file.deleteOnExit();
        settings.save(file);
        
        settings = new IpsObjectEditorSettings();
        settings.load(file);
        
        assertEquals("value1", settings.get(srcFile, "KEY1"));
        assertEquals("value2", settings.get(srcFile, "KEY2"));
        assertNull(settings.get(srcFile2, "KEY1"));
        assertEquals("value3", settings.get(srcFile3, "KEY1"));
        assertEquals("value4", settings.get(srcFile3, "KEY2"));
        assertEquals("value5", settings.get(srcFile4, "KEY1"));
        assertEquals("value6", settings.get(srcFile4, "KEY2"));
    }

}
