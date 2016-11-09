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

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.logging.Logger;

public class MailService {
	
	@Resource(lookup = "java:/AutomationMiddlewareMail")
	protected Session mailSession;

    	private static final String CONTENT_TYPE = "text/plain";

	@Inject
	private Logger log;
	
	public boolean createMessageAndSend(String subject, String content, Address[] emailReceipients) throws MessagingException{
		if (mailSession == null) {
			String addresses = "";
			if(emailReceipients != null) {
				for (Address address : emailReceipients) {
					addresses += address.toString() + ", ";
				}
			}
			log.warning("Mail session not available, unable to send Mail(subject: " +subject+", content: "+content+" ) to Receipients: " + addresses);
			return false;
		}
		
		final MimeMessage mail = new MimeMessage(mailSession);
		mail.setRecipients(Message.RecipientType.TO, emailReceipients);
		mail.setSubject(subject);
		mail.setContent(content, CONTENT_TYPE);
		mail.setSentDate(new java.util.Date());

		Transport.send(mail);
		
		return true;
	}

}
