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

package org.faktorips.devtools.core.model;

/**
 * A listener for changes to modification status changes of IPS source files.
 * 
 * @author Jan Ortmann
 */
public interface IModificationStatusChangeListener {

    /**
     * Notifies the listener that the modification status of an IPS source file has changed.
     * 
     * @param event The event with the detailed information, is never <code>null</code>.
     */
    public void modificationStatusHasChanged(ModificationStatusChangedEvent event);

}
