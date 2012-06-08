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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class MultiValueHolderTest {

    @Test
    public void testValidate_nullValue() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, null);
        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_emptyList() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);
        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());
    }

    @Test
    public void testValidate_oneValues() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);

        ArrayList<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder singleValueHolder = mock(SingleValueHolder.class);
        values.add(singleValueHolder);
        when(singleValueHolder.validate(project)).thenReturn(new MessageList());
        multiValueHolder = new MultiValueHolder(attributeValue, values);

        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());

        when(singleValueHolder.validate(project)).thenReturn(new MessageList(Message.newError("abc", "123")));

        messageList = multiValueHolder.validate(project);
        assertEquals(2, messageList.size());
        assertNotNull(messageList.getMessageByCode("abc"));
        Message messageByCode = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        assertNotNull(messageByCode);
        assertEquals(multiValueHolder, messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[0].getProperty());
    }

    @Test
    public void testValidate_multiValues() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IIpsProject project = mock(IIpsProject.class);
        when(attributeValue.getIpsProject()).thenReturn(project);
        when(attributeValue.findAttribute(project)).thenReturn(attribute);

        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue);

        ArrayList<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder singleValueHolder1 = mock(SingleValueHolder.class);
        when(singleValueHolder1.validate(project)).thenReturn(new MessageList());

        SingleValueHolder singleValueHolder2 = mock(SingleValueHolder.class);
        when(singleValueHolder2.validate(project)).thenReturn(new MessageList());

        values.add(singleValueHolder1);
        values.add(singleValueHolder2);
        multiValueHolder = new MultiValueHolder(attributeValue, values);

        MessageList messageList = multiValueHolder.validate(project);
        assertTrue(messageList.isEmpty());

        when(singleValueHolder1.validate(project)).thenReturn(new MessageList(Message.newError("abc", "123")));

        messageList = multiValueHolder.validate(project);
        assertEquals(2, messageList.size());
        assertNotNull(messageList.getMessageByCode("abc"));
        assertNotNull(messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE));

        when(singleValueHolder2.validate(project)).thenReturn(new MessageList(Message.newError("abc2", "1234")));

        messageList = multiValueHolder.validate(project);
        assertEquals(3, messageList.size());
        assertNotNull(messageList.getMessageByCode("abc"));
        assertNotNull(messageList.getMessageByCode("abc2"));
        Message messageByCode = messageList.getMessageByCode(MultiValueHolder.MSGCODE_CONTAINS_INVALID_VALUE);
        assertNotNull(messageByCode);
        assertEquals(multiValueHolder, messageByCode.getInvalidObjectProperties()[0].getObject());
        assertEquals(IValueHolder.PROPERTY_VALUE, messageByCode.getInvalidObjectProperties()[0].getProperty());
    }

    @Test
    public void testValidateDuplicateValues() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        MultiValueHolder multiValueHolder = spy(new MultiValueHolder(attributeValue));
        doNothing().when(multiValueHolder).objectHasChanged(anyObject(), anyObject());

        List<SingleValueHolder> values = new ArrayList<SingleValueHolder>();
        SingleValueHolder valueHolder = new SingleValueHolder(attributeValue, "A");
        values.add(valueHolder);
        values.add(new SingleValueHolder(attributeValue, "B"));
        values.add(new SingleValueHolder(attributeValue, "A"));
        values.add(new SingleValueHolder(attributeValue, "C"));
        multiValueHolder.setValue(values);

        MessageList messageList = multiValueHolder.validate(ipsProject).getMessages(Message.ERROR);
        assertEquals(2, messageList.getNoOfMessages(Message.ERROR));
        assertEquals(valueHolder, messageList.getMessage(0).getInvalidObjectProperties()[0].getObject());
        assertEquals(multiValueHolder, messageList.getMessage(1).getInvalidObjectProperties()[0].getObject());
    }

}