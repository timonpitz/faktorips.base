/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.IJavaPackageStructure;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.AbstractIpsBundle;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsStorage;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGeneratorBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.devtools.stdbuilder.xmodel.IGeneratedJavaElement;
import org.faktorips.devtools.stdbuilder.xmodel.ImportHandler;
import org.faktorips.devtools.stdbuilder.xmodel.ImportStatement;
import org.w3c.dom.Element;

/**
 * This class holds all the context information needed to generate the java code with our XPAND
 * builder framework. Context information are for example the java class naming or the builder
 * configuration.
 * <p>
 * The import handler for a single file build is also stored in this context and need to be reseted
 * for every new file. In fact this is not the optimum but ok for the moment. To be thread safe the
 * import handler is stored as {@link ThreadLocal} variable.
 */
public class GeneratorModelContext {

    private final JavaClassNaming javaClassNaming;

    /**
     * The import handler holds the import statements for a single file. However this context is the
     * same for all file generations. Because every file is generated sequentially in one thread we
     * could reuse a {@link ThreadLocal} variable in this model context. Every new file have to
     * clear its {@link ImportHandler} before starting generation.
     */
    private final ThreadLocal<ImportHandler> importHandlerThreadLocal = new ThreadLocal<ImportHandler>();

    private final ThreadLocal<GeneratorModelCaches> generatorModelCacheThreadLocal = new ThreadLocal<GeneratorModelCaches>();

    private final ThreadLocal<LinkedHashMap<AbstractGeneratorModelNode, List<IGeneratedJavaElement>>> generatedJavaElements = new ThreadLocal<LinkedHashMap<AbstractGeneratorModelNode, List<IGeneratedJavaElement>>>();

    private final Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap;

    private final IJavaPackageStructure javaPackageStructure;

    private final Map<IIpsPackageFragmentRoot, GeneratorConfig> generatorConfigs = new HashMap<IIpsPackageFragmentRoot, GeneratorConfig>();

    private final GeneratorConfig baseGeneratorConfig;

    public GeneratorModelContext(IIpsArtefactBuilderSetConfig config, IJavaPackageStructure javaPackageStructure,
            IIpsProject ipsProject) {
        this(config, javaPackageStructure, new HashMap<AnnotatedJavaElementType, List<IAnnotationGenerator>>(),
                ipsProject);
        annotationGeneratorMap.putAll(new AnnotationGeneratorBuilder(ipsProject).createAnnotationGenerators());
    }

    public GeneratorModelContext(IIpsArtefactBuilderSetConfig config, IJavaPackageStructure javaPackageStructure,
            Map<AnnotatedJavaElementType, List<IAnnotationGenerator>> annotationGeneratorMap, IIpsProject ipsProject) {
        this.javaPackageStructure = javaPackageStructure;
        this.annotationGeneratorMap = annotationGeneratorMap;
        this.javaClassNaming = new JavaClassNaming(javaPackageStructure, true);
        baseGeneratorConfig = new GeneratorConfig(config, ipsProject);
        for (IIpsPackageFragmentRoot packageFragmentRoot : ipsProject.getIpsPackageFragmentRoots()) {
            IIpsStorage ipsStorage = packageFragmentRoot.getIpsStorage();
            if (ipsStorage instanceof AbstractIpsBundle) {
                generatorConfigs.put(packageFragmentRoot, createConfigWithOverrides(packageFragmentRoot, ipsStorage));
            } else {
                generatorConfigs.put(packageFragmentRoot, baseGeneratorConfig);
            }
        }
    }

    private GeneratorConfig createConfigWithOverrides(IIpsPackageFragmentRoot packageFragmentRoot,
            IIpsStorage ipsStorage) {
        IIpsProject ipsProject = packageFragmentRoot.getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        IpsArtefactBuilderSetConfigModel ipsArtefactBuilderSetConfigModel = clone(properties.getBuilderSetConfig());
        overwriteProperties(ipsArtefactBuilderSetConfigModel, properties, ipsStorage);
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfig config = ipsArtefactBuilderSetConfigModel.create(ipsProject, builderSetInfo);
        return new GeneratorConfig(config, ipsProject);
    }

    private IpsArtefactBuilderSetConfigModel clone(IIpsArtefactBuilderSetConfigModel builderSetConfig) {
        Element xml = builderSetConfig.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
        IpsArtefactBuilderSetConfigModel clone = new IpsArtefactBuilderSetConfigModel();
        clone.initFromXml(xml);
        return clone;
    }

    private void overwriteProperties(IpsArtefactBuilderSetConfigModel ipsArtefactBuilderSetConfigModel,
            IIpsProjectProperties properties,
            IIpsStorage ipsStorage) {
        IpsBundleManifest bundleManifest = ((AbstractIpsBundle)ipsStorage).getBundleManifest();
        Map<String, String> generatorConfig = bundleManifest.getGeneratorConfig(properties.getBuilderSetId());
        for (Entry<String, String> entry : generatorConfig.entrySet()) {
            ipsArtefactBuilderSetConfigModel.setPropertyValue(entry.getKey(), entry.getValue(), null);
        }
    }

    /**
     * Returns the {@link GeneratorConfig} to be used when generating code for the given
     * {@link IIpsObject}. The {@link GeneratorConfig} is specific to the
     * {@link IIpsPackageFragmentRoot} the {@link IIpsObject} is contained in.
     * <p>
     * If the {@link IIpsPackageFragmentRoot} is not known to this {@link IIpsArtefactBuilderSet},
     * the {@link #getBaseGeneratorConfig()} for this {@link IIpsProject} is returned.
     */
    public GeneratorConfig getGeneratorConfig(IIpsObject ipsObject) {
        return getGeneratorConfig(ipsObject.getIpsPackageFragment());
    }

    /**
     * Returns the {@link GeneratorConfig} to be used when generating code for the
     * {@link IIpsObject} contained in the given {@link IIpsSrcFile}. The {@link GeneratorConfig} is
     * specific to the {@link IIpsPackageFragmentRoot} the {@link IIpsSrcFile} is contained in.
     * <p>
     * If the {@link IIpsPackageFragmentRoot} is not known to this {@link IIpsArtefactBuilderSet},
     * the {@link #getBaseGeneratorConfig()} for this {@link IIpsProject} is returned.
     */
    public GeneratorConfig getGeneratorConfig(IIpsSrcFile ipsSrcFile) {
        return getGeneratorConfig(ipsSrcFile.getIpsPackageFragment());
    }

    private GeneratorConfig getGeneratorConfig(IIpsPackageFragment packageFragment) {
        return getGeneratorConfig(packageFragment.getRoot());
    }

    private GeneratorConfig getGeneratorConfig(IIpsPackageFragmentRoot packageFragmentRoot) {
        GeneratorConfig generatorConfig = generatorConfigs.get(packageFragmentRoot);
        return generatorConfig != null ? generatorConfig : getBaseGeneratorConfig();
    }

    /**
     * Returns the {@link GeneratorConfig} for objects directly contained in the {@link IIpsProject}
     * this {@link GeneratorModelContext} was created with. {@link GeneratorConfig GeneratorConfigs}
     * for {@link IIpsObject IIpsObjects} contained in other {@link IIpsPackageFragmentRoot
     * IIpsPackageFragmentRoots} should be retrieved via {@link #getGeneratorConfig(IIpsObject)}.
     */
    public GeneratorConfig getBaseGeneratorConfig() {
        return baseGeneratorConfig;
    }

    /**
     * Returns the {@link GeneratorModelContext} from the {@link StandardBuilderSet} associated with
     * the element's {@link IIpsProject}.
     */
    public static GeneratorModelContext forElement(IIpsElement element) {
        IIpsProject ipsProject = element.getIpsProject();
        IIpsArtefactBuilderSet builderSet = ipsProject.getIpsArtefactBuilderSet();
        return ((StandardBuilderSet)builderSet).getGeneratorModelContext();
    }

    /**
     * Resetting the builder context for starting a new build process with clean context
     * information.
     * 
     * @param packageOfArtifacts The package of the source file to be generated to handle the
     *            correct import statements
     */
    public void resetContext(String packageOfArtifacts) {
        importHandlerThreadLocal.set(new ImportHandler(packageOfArtifacts));
        generatorModelCacheThreadLocal.set(new GeneratorModelCaches());
        generatedJavaElements.set(new LinkedHashMap<AbstractGeneratorModelNode, List<IGeneratedJavaElement>>());
    }

    /**
     * Returns the thread local import handler. The import handler stores all import statements
     * needed in the generated class file.
     * <p>
     * The import handler is stored as {@link ThreadLocal} variable to have the ability to generate
     * different files in different threads
     * <p>
     * To be able to use the generator model nodes also if no build process is running, this method
     * would return a new import handler in the case there is no import handler yet.
     * 
     * @return The thread local import handler
     */
    public ImportHandler getImportHandler() {
        ImportHandler importHandler = importHandlerThreadLocal.get();
        if (importHandler != null) {
            return importHandler;
        } else {
            return new ImportHandler(StringUtils.EMPTY);
        }
    }

    /**
     * Sets the thread local import handler. The import handler stores all import statements needed
     * in the generated class file.
     * <p>
     * The import handler is stored as {@link ThreadLocal} variable to have the ability to generate
     * different files in different threads
     * 
     * @param importHandler The thread local import handler
     */
    protected void setImportHandler(ImportHandler importHandler) {
        this.importHandlerThreadLocal.set(importHandler);
    }

    /**
     * Returns the thread local generator model cache. The generator model cache stores all cached
     * object references that may change on any time.
     * <p>
     * The generator model cache is stored as {@link ThreadLocal} variable to have the ability to
     * generate different files in different threads
     * 
     * @return The thread local generator model cache
     */
    public GeneratorModelCaches getGeneratorModelCache() {
        return generatorModelCacheThreadLocal.get();
    }

    /**
     * Getting the set of collected import statements.
     * 
     * @return Returns the imports.
     */
    public Set<ImportStatement> getImports() {
        return getImportHandler().getImports();
    }

    /**
     * Adds a new import. The import statement should be the full qualified name of a class.
     * 
     * @param importStatement The full qualified name of a class that should be imported.
     * @return the qualified or unqualified class name depending on whether it is required.
     * @see ImportHandler#addImportAndReturnClassName(String)
     */
    public String addImport(String importStatement) {
        return getImportHandler().addImportAndReturnClassName(importStatement);
    }

    public boolean removeImport(String importStatement) {
        return getImportHandler().remove(importStatement);
    }

    /**
     * Returns the thread local generated artifacts map that maps a
     * {@link AbstractGeneratorModelNode} to a list of generated java elements.
     * <p>
     * The map is stored as {@link ThreadLocal} variable to have the ability to generate different
     * files in different threads.
     * 
     * @return The thread local map holding the generated java elements for the generator model
     *         nodes
     */
    private LinkedHashMap<AbstractGeneratorModelNode, List<IGeneratedJavaElement>> getGeneratedJavaElementsMap() {
        return generatedJavaElements.get();
    }

    /**
     * Add a generated java element to the list of generated elements for the specified generator
     * model node
     * 
     * @param node The generator model node that generates the specified element
     * @param element the generated java element
     */
    public void addGeneratedJavaElement(AbstractGeneratorModelNode node, IGeneratedJavaElement element) {
        List<IGeneratedJavaElement> list = getGeneratedJavaElements(node);
        list.add(element);
    }

    /**
     * Returns the list of generated java elements that is stored for the specified
     * {@link AbstractGeneratorModelNode}. The list is stored in a {@link ThreadLocal}.
     * 
     * @param node The generator model node for which you want to have the generated artifacts
     * @return The list of generated java elements for the specified generator model nodes
     */
    public List<IGeneratedJavaElement> getGeneratedJavaElements(AbstractGeneratorModelNode node) {
        List<IGeneratedJavaElement> list = getGeneratedJavaElementsMap().get(node);
        if (list == null) {
            list = new ArrayList<IGeneratedJavaElement>();
            getGeneratedJavaElementsMap().put(node, list);
        }
        return list;
    }

    /**
     * Returns the list of annotation generators for the given type. This method never returns null.
     * If there is no annotation generator for the specified type an empty list will be returned.
     * 
     * @param type The {@link AnnotatedJavaElementType} you want to get the generators for
     * @return the list of {@link IAnnotationGenerator annotation generators} or an empty list if
     *         there is none
     */
    public List<IAnnotationGenerator> getAnnotationGenerator(AnnotatedJavaElementType type) {
        List<IAnnotationGenerator> result = annotationGeneratorMap.get(type);
        if (result == null) {
            result = new ArrayList<IAnnotationGenerator>();
        }
        return result;
    }

    public JavaClassNaming getJavaClassNaming() {
        return javaClassNaming;
    }

    public String getValidationMessageBundleBaseName(IIpsSrcFolderEntry entry) {
        String baseName = javaPackageStructure.getBasePackageName(entry, true, false) + "."
                + entry.getValidationMessagesBundle();
        return baseName;
    }

}
