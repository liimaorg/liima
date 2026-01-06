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

import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.utils.notification.MailService;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

	@InjectMocks
	MailService mailService;

	@Mock
	Logger log;
	

	
	@Test
	public void test_createMessageAndSend_logging_null() throws MessagingException {
		// given
		// Mail Session is null
		
		// when
		mailService.createMessageAndSend("subject", "content", null);
		
		// then
		verify(log, times(1)).warning("Mail session not available, unable to send Mail(subject: subject, content: content ) to Recipients: ");
	}
	
	@Test
	public void test_createMessageAndSend_logging_Empty() throws MessagingException {
		// given
		// Mail Session is null
		Address[] to = new InternetAddress[0];
		
		// when
		mailService.createMessageAndSend("subject", "content", to);
		
		// then
		verify(log, times(1)).warning("Mail session not available, unable to send Mail(subject: subject, content: content ) to Recipients: ");
	}
	
	@Test
	public void test_createMessageAndSend_logging() throws MessagingException {
		// given
		// Mail Session is null
		Address[] to = new InternetAddress[1];
		to[0] = new InternetAddress("test@test.ch");
		
		// when
		mailService.createMessageAndSend("subject", "content", to);
		
		// then
		verify(log, times(1)).warning("Mail session not available, unable to send Mail(subject: subject, content: content ) to Recipients: test@test.ch, ");
	}

}
