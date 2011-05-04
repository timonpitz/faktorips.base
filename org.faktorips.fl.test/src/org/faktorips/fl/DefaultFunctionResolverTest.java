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

package org.faktorips.fl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.functions.AbstractFlFunction;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class DefaultFunctionResolverTest {

    private DefaultFunctionResolver resolver;

    @Before
    public void setUp() throws Exception {
        resolver = new DefaultFunctionResolver();
    }

    @Test
    public void testAdd() {
        FlFunction fct = new AbstractTestFlFunction("fct1", Datatype.DECIMAL, new Datatype[0]);
        resolver.add(fct);
        assertEquals(1, resolver.getFunctions().length);
        assertSame(fct, resolver.getFunctions()[0]);
    }

    @Test
    public void testRemove() {
        AbstractTestFlFunction fct1 = new AbstractTestFlFunction("fct1", Datatype.DECIMAL, new Datatype[0]);
        resolver.add(fct1);
        resolver.remove(fct1);
        assertEquals(0, resolver.getFunctions().length);

        resolver.remove(fct1); // should do nothing
    }

    static class AbstractTestFlFunction extends AbstractFlFunction {

        // result to be returned.
        private CompilationResult result;

        AbstractTestFlFunction(String name, Datatype type, Datatype[] argTypes) {
            super(name, "", type, argTypes);
        }

        public CompilationResult compile(CompilationResult[] argResults) {
            return result;
        }
    }
}