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

package ch.puzzle.itc.mobiliar.business.utils.notification;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;

import ch.puzzle.itc.mobiliar.common.util.ConfigKey;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;

@Stateless
public class NotificationService {
	
	@Inject
	private MailService mailService;
	
	/**
	 * creates and sends an Email to the given emailRecipients
	 * 
	 * @param subject
	 * @param content
	 * @param emailRecipients
	 * @return
	 * @throws MessagingException
	 */
	public boolean createAndSendMail(String subject, String content, Address[] emailRecipients) throws MessagingException{
		boolean deliverEmails = ConfigurationService.getPropertyAsBoolean(ConfigKey.DELIVER_MAIL, true);
		// deliverEmails is active and emailRecipients are available
		if (deliverEmails && emailRecipients != null && emailRecipients.length > 0) {
			return mailService.createMessageAndSend(subject, content, emailRecipients);
		}
		return false;
	}
}
