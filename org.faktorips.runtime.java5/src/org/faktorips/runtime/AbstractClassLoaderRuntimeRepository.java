/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.faktorips.runtime.internal.AbstractRuntimeRepository;
import org.faktorips.runtime.internal.AbstractTocBasedRuntimeRepository;
import org.faktorips.runtime.internal.EnumSaxHandler;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.internal.toc.GenerationTocEntry;
import org.faktorips.runtime.internal.toc.IEnumContentTocEntry;
import org.faktorips.runtime.internal.toc.IProductCmptTocEntry;
import org.faktorips.runtime.internal.toc.ITableContentTocEntry;
import org.faktorips.runtime.internal.toc.ITestCaseTocEntry;
import org.faktorips.runtime.test.IpsTestCase2;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public abstract class AbstractClassLoaderRuntimeRepository extends AbstractTocBasedRuntimeRepository {

    private final ClassLoader cl;

    public AbstractClassLoaderRuntimeRepository(String name, ICacheFactory cacheFactory, ClassLoader cl) {
        super(name, cacheFactory);
        this.cl = cl;
    }

    @Override
    protected IProductComponent createProductCmpt(IProductCmptTocEntry tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        ProductComponent productCmpt;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] { IRuntimeRepository.class, String.class,
                    String.class, String.class });
            productCmpt = (ProductComponent)constructor.newInstance(new Object[] { this, tocEntry.getIpsObjectId(),
                    tocEntry.getKindId(), tocEntry.getVersionId() });
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        Element docElement = getDocumentElement(tocEntry);
        productCmpt.initFromXml(docElement);
        return productCmpt;
    }

    @Override
    protected <T> List<T> createEnumValues(IEnumContentTocEntry tocEntry, Class<T> clazz) {
        InputStream is = getXmlAsStream(tocEntry);

        EnumSaxHandler saxhandler = new EnumSaxHandler();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), saxhandler);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse the enumeration content of the resource "
                    + tocEntry.getXmlResourceName());
        }
        T enumValue = null;
        ArrayList<T> enumValues = new ArrayList<T>();
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<T> constructor = null;
            for (Constructor<?> currentConstructor : constructors) {
                if ((currentConstructor.getModifiers() & Modifier.PROTECTED) > 0) {
                    Class<?>[] parameterTypes = currentConstructor.getParameterTypes();
                    if (parameterTypes.length == 2 && parameterTypes[0] == List.class
                            && parameterTypes[1] == IRuntimeRepository.class) {
                        @SuppressWarnings("unchecked")
                        // neccessary as Class.getDeclaredConstructors() is of type Constructor<?>[]
                        // while returning Contructor<T>[]
                        // The Javaoc Class.getDeclaredConstructors() for more information
                        Constructor<T> castedConstructor = (Constructor<T>)currentConstructor;
                        constructor = castedConstructor;
                    }
                }
            }
            if (constructor == null) {
                throw new RuntimeException(
                        "No valid constructor found to create enumerations instances for the toc entry " + tocEntry);
            }
            for (List<String> enumValueAsStrings : saxhandler.getEnumValueList()) {
                constructor.setAccessible(true);
                enumValue = constructor.newInstance(new Object[] { enumValueAsStrings, this });
                enumValues.add(enumValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't create enumeration instance for toc entry " + tocEntry, e);
        }
        return enumValues;
    }

    @Override
    protected IProductComponentGeneration createProductCmptGeneration(GenerationTocEntry tocEntry) {
        ProductComponent productCmpt = (ProductComponent)getProductComponent(tocEntry.getParent().getIpsObjectId());
        if (productCmpt == null) {
            throw new RuntimeException("Can't get product component for toc entry " + tocEntry);
        }
        ProductComponentGeneration productCmptGen;
        try {
            Constructor<?> constructor = getConstructor(tocEntry);
            productCmptGen = (ProductComponentGeneration)constructor.newInstance(new Object[] { productCmpt });
        } catch (Exception e) {
            throw new RuntimeException("Can't create product component instance for toc entry " + tocEntry, e);
        }
        Element genElement = getDocumentElement(tocEntry);
        productCmptGen.initFromXml(genElement);
        return productCmptGen;
    }

    private Constructor<?> getConstructor(GenerationTocEntry tocEntry) {
        Class<?> implClass = getClass(getProductComponentGenerationImplClass(tocEntry), cl);
        try {
            String productCmptClassName = tocEntry.getParent().getImplementationClassName();
            Class<?> productCmptClass = getClass(productCmptClassName, cl);
            return implClass.getConstructor(new Class[] { productCmptClass });
        } catch (Exception e) {
            throw new RuntimeException("Can't get constructor for class " + implClass.getName() + " , toc entry "
                    + tocEntry, e);
        }
    }

    /**
     * More efficient implementation of
     * {@link AbstractRuntimeRepository#getAllProductComponents(Class)}
     */
    @Override
    public List<IProductComponent> getAllProductComponents(Class<?> productCmptClass) {
        List<IProductComponent> result = new ArrayList<IProductComponent>();
        List<IProductCmptTocEntry> entries = toc.getProductCmptTocEntries();
        for (IProductCmptTocEntry entry : entries) {
            Class<?> clazz = getClass(entry.getImplementationClassName(), getClassLoader());
            if (productCmptClass.isAssignableFrom(clazz)) {
                result.add(getProductComponentInternal(entry));
            }
        }
        return result;
    }

    @Override
    protected ITable createTable(ITableContentTocEntry tocEntry) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        Table table;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[0]);
            table = (Table)constructor.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Can't create table instance for toc entry " + tocEntry, e);
        }

        InputStream is = getXmlAsStream(tocEntry);

        try {
            table.initFromXml(is, this);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse xml for " + tocEntry.getIpsObjectId(), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to close the input stream for : " + tocEntry.getIpsObjectId(), e);
            }
        }
        return table;
    }

    @Override
    protected IpsTestCaseBase createTestCase(ITestCaseTocEntry tocEntry, IRuntimeRepository runtimeRepository) {
        Class<?> implClass = getClass(tocEntry.getImplementationClassName(), getClassLoader());
        IpsTestCaseBase test;
        try {
            Constructor<?> constructor = implClass.getConstructor(new Class[] { String.class });
            test = (IpsTestCaseBase)constructor.newInstance(new Object[] { tocEntry.getIpsObjectQualifiedName() });
        } catch (Exception e) {
            throw new RuntimeException("Can't create test case instance for toc entry " + tocEntry, e);
        }
        /*
         * sets the runtime repository which will be used to instantiate the test case, this could
         * be a different one (e.g. contains more dependence repositories) as the test case belongs
         * to, because the test case itself could contain objects from different repositories, the
         * runtime repository should contain all needed repositories
         */
        test.setRepository(runtimeRepository);
        if (test instanceof IpsTestCase2) {
            // only classes of type ips test case 2 supports xml input
            Element docElement = getDocumentElement(tocEntry);
            ((IpsTestCase2)test).initFromXml(docElement);
        }
        test.setFullPath(tocEntry.getIpsObjectId());
        return test;
    }

    @Override
    public ClassLoader getClassLoader() {
        return cl;
    }

    protected abstract Element getDocumentElement(IProductCmptTocEntry tocEntry);

    protected abstract Element getDocumentElement(GenerationTocEntry tocEntry);

    protected abstract Element getDocumentElement(ITestCaseTocEntry tocEntry);

    protected abstract String getProductComponentGenerationImplClass(GenerationTocEntry tocEntry);

    /**
     * Returns the XML data for the specified tocEntry as {@link InputStream}
     * 
     * @param tocEntry Specifying the requested EnumContent
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(IEnumContentTocEntry tocEntry);

    /**
     * Returns the XML data for the specified tocEntry as {@link InputStream}
     * 
     * @param tocEntry Specifying the requested TableContent
     * @return An InputStream containing the XML data - should not return null!
     * @throws RuntimeException in case of any exception do not return null but an accurate
     *             {@link RuntimeException}
     */
    protected abstract InputStream getXmlAsStream(ITableContentTocEntry tocEntry);

}