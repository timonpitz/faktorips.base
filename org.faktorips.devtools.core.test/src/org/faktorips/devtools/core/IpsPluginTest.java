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

package org.faktorips.devtools.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.faktorips.devtools.core.model.IIpsLoggingFrameworkConnector;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsPluginTest extends AbstractIpsPluginTest {

    private IpsPreferences pref;
    private String oldPresentationString;
    
    public IpsPluginTest() {
        super();
    }

    public IpsPluginTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        pref = IpsPlugin.getDefault().getIpsPreferences();
        oldPresentationString = pref.getNullPresentation(); 
    }
    
    protected void tearDown() {
        pref.setNullPresentation(oldPresentationString);
    }

    public void testIpsPreferencesInclListener() {
        
        MyPropertyChangeListener listener = new MyPropertyChangeListener();
        IPreferenceStore store = IpsPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(listener);
        pref.setNullPresentation("-");
        assertEquals("-", pref.getNullPresentation());
        assertNotNull(listener.lastEvent);
        assertEquals("-", listener.lastEvent.getNewValue());
    }
    
    class MyPropertyChangeListener implements IPropertyChangeListener {
        
        PropertyChangeEvent lastEvent;
        
        public void propertyChange(PropertyChangeEvent event) {
            lastEvent = event;
        }
    }
    
    public void testGetIpsLoggingFrameworkConnectors() throws CoreException{
        IIpsLoggingFrameworkConnector[] connectors = IpsPlugin.getDefault().getIpsLoggingFrameworkConnectors();
        List connectorIds = new ArrayList();
        for (int i = 0; i < connectors.length; i++) {
            connectorIds.add(connectors[i].getId());
        }
        assertTrue(connectorIds.contains("org.faktorips.devtools.core.javaUtilLoggingConnector"));
    }
    
    public void testGetIpsFeatureVersion(){
        System.out.println(IpsPlugin.getDefault().getIpsFeatureVersion());
    }
}
