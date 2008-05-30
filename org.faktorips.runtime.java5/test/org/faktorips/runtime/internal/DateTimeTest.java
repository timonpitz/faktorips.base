/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class DateTimeTest extends TestCase {

    /*
     * Test method for 'org.faktorips.runtime.internal.DateTime.toDate(TimeZone)'
     */
    public void testToDate() {
        DateTime dt = new DateTime(2005, 5, 1);
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        Date date = cal.getTime();
        assertEquals(date, dt.toDate(cal.getTimeZone()));
    }

    /*
     * Test method for 'org.faktorips.runtime.internal.DateTime.toGregorianCalendar(TimeZone)'
     */
    public void testToGregorianCalendar() {
        DateTime dt = new DateTime(2005, 5, 1);
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        assertEquals(cal, dt.toGregorianCalendar(cal.getTimeZone()));
    }

    /*
     * Test method for 'org.faktorips.runtime.internal.DateTime.toGregorianCalendar(TimeZone)'
     */
    public void testCreateDateOnly() {
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        assertEquals(new DateTime(2005, 5, 1), DateTime.createDateOnly(cal));
        assertNull(DateTime.createDateOnly(null));
    }
}
