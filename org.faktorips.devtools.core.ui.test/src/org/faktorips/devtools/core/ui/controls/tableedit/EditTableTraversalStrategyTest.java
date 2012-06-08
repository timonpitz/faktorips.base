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

package org.faktorips.devtools.core.ui.controls.tableedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EditTableTraversalStrategyTest {

    private static final String A = "A";
    private static final String B1 = "B";
    private static final String C = "C";
    /*
     * Force new String instance as items B1 and B2 should be equal but not same. The search in the
     * models list should be based on identity not equality.
     */
    private static final String B2 = new String("B");
    private EditTableTraversalStrategy strat;
    private List<String> list;

    @Before
    public void setUp() {
        list = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        AbstractListTableModel<String> model = mock(AbstractListTableModel.class);
        doReturn(list).when(model).getElements();
        strat = new EditTableTraversalStrategy(null, 0, model);
    }

    @Test
    public void testGetNextVisibleViewItem() {
        list.add(A);
        list.add(B1);
        list.add(C);
        list.add(B2);
        assertEquals(B1, strat.getNextVisibleViewItem(A));
        assertEquals(C, strat.getNextVisibleViewItem(B1));
        assertEquals(B2, strat.getNextVisibleViewItem(C));
        // value "B" is found at index 1, next item is C
        assertEquals(C, strat.getNextVisibleViewItem(B2));
        assertEquals(A, strat.getNextVisibleViewItem("inexistent"));
    }

    @Test
    public void testGetNextVisibleViewItem_Empty() {
        assertNull(strat.getNextVisibleViewItem("inexistent"));
    }

    @Test
    public void testGetPreviousVisibleViewItem() {
        list.add(A);
        list.add(B1);
        list.add(C);
        list.add(B2);
        assertNull(strat.getPreviousVisibleViewItem(A));
        assertEquals(A, strat.getPreviousVisibleViewItem(B1));
        assertEquals(B1, strat.getPreviousVisibleViewItem(C));
        // value "B" is found at index 1, previous item is A
        assertEquals(A, strat.getPreviousVisibleViewItem(B2));
        assertEquals(A, strat.getNextVisibleViewItem("inexistent"));
    }

    @Test
    public void testGetPreviousVisibleViewItem_Empty() {
        assertNull(strat.getPreviousVisibleViewItem("inexistent"));
    }

}