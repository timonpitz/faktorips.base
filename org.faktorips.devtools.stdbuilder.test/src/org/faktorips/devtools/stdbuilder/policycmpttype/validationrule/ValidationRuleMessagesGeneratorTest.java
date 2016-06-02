/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.SupportedLanguage;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;

public class ValidationRuleMessagesGeneratorTest extends AbstractIpsPluginTest {

    private static final String RULE_NAME_1 = "rule1";
    private static final String RULE_NAME_2 = "rule2";
    private static final String MY_QNAME = "myQName";
    private static final String QNAME_RULE1 = MY_QNAME + IValidationRule.QNAME_SEPARATOR + RULE_NAME_1;

    private IPolicyCmptType pcType;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        pcType = newPolicyCmptType(ipsProject, MY_QNAME);
    }

    @Test
    public void testLoadMessagesFromFile() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        when(propertyFile.getContents()).thenReturn(inputStream);

        new ValidationRuleMessagesGenerator(propertyFile, new SupportedLanguage(Locale.GERMAN), builder);

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);

        when(propertyFile.exists()).thenReturn(true);

        new ValidationRuleMessagesGenerator(propertyFile, new SupportedLanguage(Locale.GERMAN), builder);

        verify(propertyFile).getContents();
        verify(inputStream).close();
    }

    @Test
    public void testGenerate() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        InputStream inputStream = mock(InputStream.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);
        ValidationRuleMessageProperties validationMessages = messagesGenerator.getValidationMessages();

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
        assertFalse(validationMessages.isModified());

        messagesGenerator.generate(pcType);

        assertFalse(validationMessages.isModified());

        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, "anyMessage"));

        IValidationRule validationRule2 = pcType.newRule();
        validationRule2.setName(RULE_NAME_2);
        validationRule2.getMessageText().add(new LocalizedString(Locale.GERMAN, "anyMessage"));

        messagesGenerator.generate(pcType);
        assertTrue(validationMessages.isModified());
        assertEquals(2, validationMessages.size());

        messagesGenerator.saveIfModified("");

        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));

        reset(propertyFile);
        reset(inputStream);

        messagesGenerator.generate(pcType);
        assertFalse(validationMessages.isModified());

        verifyZeroInteractions(propertyFile);
        verifyZeroInteractions(inputStream);
    }

    @Test
    public void testSafeIfModified() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        List<IValidationRule> vRulesList = new ArrayList<IValidationRule>();
        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, "anyMessage"));

        vRulesList.add(validationRule1);
        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));
    }

    @Test
    public void testSafeIfModified_notModified() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        IFile propertyFile = mock(IFile.class);
        ValidationRuleMessagesGenerator messagesGenerator = new ValidationRuleMessagesGenerator(propertyFile,
                new SupportedLanguage(Locale.GERMAN), builder);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verifyNoMoreInteractions(propertyFile);

        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, "anyMessage"));

        messagesGenerator.generate(pcType);

        reset(propertyFile);

        messagesGenerator.saveIfModified("");

        verify(propertyFile).exists();
        verify(propertyFile).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));

        messagesGenerator.loadMessages();
        messagesGenerator.generate(pcType);
        messagesGenerator.saveIfModified("");

        verify(propertyFile, never()).setContents(any(InputStream.class), anyBoolean(), anyBoolean(),
                any(NullProgressMonitor.class));
    }

    @Test
    public void testDeleteMessagesForDeletedRules() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN), builder);

        IPolicyCmptType pcType2 = newPolicyCmptType(ipsProject, "pcType2");

        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, "text1"));

        IValidationRule validationRule2 = pcType.newRule();
        validationRule2.setName(RULE_NAME_2);
        validationRule2.getMessageText().add(new LocalizedString(Locale.GERMAN, "text2"));

        IValidationRule otherRule = pcType2.newRule();
        otherRule.setName("otherRule");
        otherRule.getMessageText().add(new LocalizedString(Locale.GERMAN, "text3"));

        validationRuleMessagesGenerator.addValidationRuleMessages(pcType);
        validationRuleMessagesGenerator.addValidationRuleMessages(pcType2);

        validationRule2.delete();
        validationRuleMessagesGenerator.deleteMessagesForDeletedRules(pcType);

        assertEquals(2, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals(
                "text1",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(
                        validationRule1.getQualifiedRuleName()));
        assertEquals("text3",
                validationRuleMessagesGenerator.getValidationMessages().getMessage(otherRule.getQualifiedRuleName()));
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN), builder);

        IPolicyCmptType pcType2 = newPolicyCmptType(ipsProject, "pcType2");

        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, "text1"));

        IValidationRule validationRule2 = pcType.newRule();
        validationRule2.setName(RULE_NAME_2);
        validationRule2.getMessageText().add(new LocalizedString(Locale.GERMAN, "text2"));

        IValidationRule otherRule = pcType2.newRule();
        otherRule.setName("otherRule");
        otherRule.getMessageText().add(new LocalizedString(Locale.GERMAN, "text3"));

        validationRuleMessagesGenerator.addValidationRuleMessages(pcType);
        validationRuleMessagesGenerator.addValidationRuleMessages(pcType2);

        validationRuleMessagesGenerator.deleteAllMessagesFor(MY_QNAME);

        assertEquals(1, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals("text3", validationRuleMessagesGenerator.getValidationMessages().getMessage("pcType2-otherRule"));
    }

    @Test
    public void testAddValidationRuleMessage_emptyMessage() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN), builder);

        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, ""));
        validationRuleMessagesGenerator.addValidationRuleMessages(pcType);

        assertFalse(validationRuleMessagesGenerator.getValidationMessages().isModified());
        assertEquals(0, validationRuleMessagesGenerator.getValidationMessages().size());
    }

    @Test
    public void testAddValidationRuleMessage_emptyMessageDefaultLang() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(Locale.GERMAN, true), builder);

        IValidationRule validationRule1 = pcType.newRule();
        validationRule1.setName(RULE_NAME_1);
        validationRule1.getMessageText().add(new LocalizedString(Locale.GERMAN, ""));
        validationRuleMessagesGenerator.addValidationRuleMessages(pcType);

        assertTrue(validationRuleMessagesGenerator.getValidationMessages().isModified());
        assertEquals(1, validationRuleMessagesGenerator.getValidationMessages().size());
        assertEquals("", validationRuleMessagesGenerator.getValidationMessages().getMessage(QNAME_RULE1));
    }

    @Test
    public void testGetMessageText() throws Exception {
        ValidationRuleMessagesPropertiesBuilder builder = mock(ValidationRuleMessagesPropertiesBuilder.class);
        Locale locale = Locale.GERMAN;
        ValidationRuleMessagesGenerator validationRuleMessagesGenerator = new ValidationRuleMessagesGenerator(
                mock(IFile.class), new SupportedLanguage(locale), builder);

        IValidationRule validationRule = pcType.newRule();

        validationRule.getMessageText().add(new LocalizedString(locale, ""));
        String result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("", result);

        validationRule.getMessageText().add(new LocalizedString(locale, "Abc 123 alles klar"));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar", result);

        validationRule.getMessageText().add(new LocalizedString(locale, "Anc {abc123} afs"));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Anc {0} afs", result);

        validationRule.getMessageText().add(new LocalizedString(locale, "Abc 123 alles klar {peter} usw."));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("Abc 123 alles klar {0} usw.", result);

        validationRule.getMessageText().add(new LocalizedString(locale, "x{0} Abc 123 alles klar {1} usw."));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("x{0} Abc 123 alles klar {1} usw.", result);

        validationRule.getMessageText().add(new LocalizedString(locale, "{abc} Abc 123 alles klar {xyz} usw."));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw.", result);

        validationRule.getMessageText().add(
                new LocalizedString(locale, "{abc,number} Abc 123 alles klar {xyz, date, long} usw."));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0,number} Abc 123 alles klar {1, date, long} usw.", result);

        // same parameter multiple times
        validationRule.getMessageText().add(
                new LocalizedString(locale, "{abc} Abc 123 alles klar {xyz} usw. blabla {xyz} asd {abc} soso"));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} Abc 123 alles klar {1} usw. blabla {1} asd {0} soso", result);

        // parameter with underscore
        validationRule.getMessageText().add(new LocalizedString(locale, "{abc_xyz} asdfsdaf"));
        result = validationRuleMessagesGenerator.getMessageText(validationRule);
        assertEquals("{0} asdfsdaf", result);

    }

}
