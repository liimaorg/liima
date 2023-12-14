/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.utils.notification.mail;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import ch.puzzle.itc.mobiliar.business.utils.notification.NotificationService;
import ch.puzzle.itc.mobiliar.business.utils.notification.MailService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.puzzle.itc.mobiliar.common.util.ConfigKey;

public class NotificationServiceTest {

	@InjectMocks
	NotificationService notificationService;
	
	@Mock
	MailService mailService;
	
	
	
	@Before
	public void setUp(){
		MockitoAnnotations.openMocks(this);
	}
	
	
	@Test
	public void test_createAndSendMail_noEmailRecipients() throws MessagingException {
		// given
		
		// when
		boolean result = notificationService.createAndSendMail("subject", "content", null);
		
		// then
		assertFalse(result);
	}
	
	@Test
	public void test_createAndSendMail_noEmailRecipients_emptyList() throws MessagingException {
		// given
		Address[] to = new InternetAddress[0];
		// when
		boolean result = notificationService.createAndSendMail("subject", "content", to);
		
		// then
		assertFalse(result);
	}
	
	@Test
	public void test_createAndSendMail_noEmailRecipients_DeliverEmailsFalse() throws MessagingException {
		// given
		Properties props = System.getProperties();
		props.setProperty(ConfigKey.DELIVER_MAIL.getValue(), "false");
		
		// when
		boolean result = notificationService.createAndSendMail("subject", "content", null);
		
		// then
		assertFalse(result);
		
		props.remove(ConfigKey.DELIVER_MAIL.getValue());
	}
	
	@Test
	public void test_createAndSendMail_ok() throws MessagingException {
		// given
		Address[] to = new InternetAddress[1];
		to[0] = new InternetAddress("test@test.ch");
		
		when(mailService.createMessageAndSend("subject", "content", to)).thenReturn(true);
		
		// when
		boolean result = notificationService.createAndSendMail("subject", "content", to);
		
		// then
		verify(mailService, times(1)).createMessageAndSend("subject", "content", to);
		assertTrue(result);
	}
	
	@Test
	public void test_createAndSendMail_nok() throws MessagingException {
		// given
		Address[] to = new InternetAddress[1];
		to[0] = new InternetAddress("test@test.ch");
		
		when(mailService.createMessageAndSend("subject", "content", to)).thenReturn(false);
		
		// when
		boolean result = notificationService.createAndSendMail("subject", "content", to);
		
		// then
		verify(mailService, times(1)).createMessageAndSend("subject", "content", to);
		assertFalse(result);
	}

}
