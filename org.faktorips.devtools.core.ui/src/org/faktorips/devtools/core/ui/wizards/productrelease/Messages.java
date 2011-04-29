/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productrelease;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.wizards.productrelease.messages"; //$NON-NLS-1$
    public static String ProductReleaserBuilderWizard_complete_error;
    public static String ProductReleaserBuilderWizard_complete_aborted;
    public static String ProductReleaserBuilderWizard_complete_success;
    public static String ReleaserBuilderWizard_title;
    public static String ReleaserBuilderWizard_exception_NotReady;
    public static String ReleaserBuilderWizardSelectionPage_error_couldNotDetermineFormat;
    public static String ReleaserBuilderWizardSelectionPage_error_illegalVersion;
    public static String ReleaserBuilderWizardSelectionPage_error_noDeploymentExtension;
    public static String ReleaserBuilderWizardSelectionPage_error_versionFormat;
    public static String ReleaserBuilderWizardSelectionPage_group_project;
    public static String ReleaserBuilderWizardSelectionPage_group_targetsystem;
    public static String ReleaserBuilderWizardSelectionPage_group_version;
    public static String ReleaserBuilderWizardSelectionPage_info_selectProject;
    public static String ReleaserBuilderWizardSelectionPage_latest_version;
    public static String ReleaserBuilderWizardSelectionPage_new_version;
    public static String ReleaserBuilderWizardSelectionPage_select_project;
    public static String ReleaserBuilderWizardSelectionPage_title;
    public static String ReleaserBuilderWizardSelectionPage_warning_sameVersion;
    public static String ProductReleaserBuilderWizard_exception_unsavedChanges;
    public static String UsernamePasswordDialog_password;
    public static String UsernamePasswordDialog_prompt;
    public static String UsernamePasswordDialog_savePassword;
    public static String UsernamePasswordDialog_username;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do not initialize
    }
}
