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

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;

/**
 * This FolderExplorer supports reading files and subfolders of folders.
 * 
 * 
 * @author dicker
 */
public class FolderExplorer {

    /**
     * returns a List of {@link IPath paths}, which represents the absolute paths to the files
     * within the given {@link IPath}, which are not folders.
     * <p>
     * If nothing is found, an empty List will be returned.
     * 
     */
    public List<IPath> getFiles(IPath path) {
        if (isNotPathRelevant(path)) {
            return Collections.emptyList();
        }

        return createPaths(path, false);
    }

    /**
     * returns a List of {@link IPath paths}, which represents the absolute paths to the folders
     * within the given {@link IPath}.
     * <p>
     * If nothing is found, an empty List will be returned.
     * 
     */
    public List<IPath> getFolders(IPath path) {
        if (isNotPathRelevant(path)) {
            return Collections.emptyList();
        }
        return createPaths(path, true);
    }

    private boolean isRelevant(File file, boolean mustBeDirectory) {
        if (mustBeDirectory) {
            return file.isDirectory();
        }
        return file.isFile();
    }

    private List<IPath> createPaths(IPath path, boolean mustBeDirectory) {
        List<IPath> paths = new ArrayList<IPath>();
        File[] listFiles = path.toFile().listFiles();
        for (File file : listFiles) {
            if (isRelevant(file, mustBeDirectory)) {
                String name = file.getName();
                paths.add(path.append(name));
            }
        }

        return paths;
    }

    private boolean isNotPathRelevant(IPath path) {
        if (path == null) {
            return true;
        }

        File file = path.toFile();
        if (!file.isDirectory()) {
            return true;
        }

        File[] listFiles = file.listFiles();
        if (listFiles.length == 0) {
            return true;
        }

        return false;
    }
}