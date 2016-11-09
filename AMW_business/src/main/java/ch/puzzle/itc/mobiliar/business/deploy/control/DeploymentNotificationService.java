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

package ch.puzzle.itc.mobiliar.business.deploy.control;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.usersettings.control.UserSettingsService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.utils.notification.NotificationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService.ConfigKey;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class DeploymentNotificationService {

	@Inject
	private NotificationService notificationService;

	@Inject
	private UserSettingsService userSettingsService;

	@Inject
	private ResourceDependencyResolverService resourceDependencyResolverService;

	@Inject
	private ReleaseMgmtService releaseMgmtService;

	@Inject
	private Logger log;

	/**
	 * Creates a notification email for deployment executions and sends them to
	 * the given receipients
	 *
	 * @param deployments
	 *            - the executed deployments
	 * @return the message to be stored with the deployment.
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public String createAndSendMailForDeplyoments(
			final List<DeploymentEntity> deployments) {
		String message = null;

		// do nothing if no deployment is available
		if (deployments != null && !deployments.isEmpty()) {
			String subjectMessage = "AMW-Deploy for tracking id: "
					+ deployments.get(0).getTrackingId();

			try {
				Address[] emailReceipients = getAllReceipients(deployments);

				if (notificationService.createAndSendMail(subjectMessage,
						getMessageContentForDeployments(deployments),
						emailReceipients)) {
					message = getSuccessfulSendMailToReceipientMessage(emailReceipients);
				}
			} catch (MessagingException e) {
				message = getFailureSendMailMessage(e.getMessage());
				log.log(Level.WARNING, "Deployment notification ("
						+ subjectMessage + ") could not be sent", e);
			}
		}

		return message;
	}

	/**
	 * Creation of the e-mail content
	 * 
	 * @param deployments
	 *            - the deployments for which the email shall be generated.
	 * @return the e-mail content to be sent
	 */
	private String getMessageContentForDeployments(
			final List<DeploymentEntity> deployments) {
		final StringBuffer message = new StringBuffer();
		for (final DeploymentEntity deployment : deployments) {

			message.append("AMW-Deployment: ").append(deployment.getId()).append("(").append(deployment.getResourceGroup().getName())
			.append(" on ").append(deployment.getContext().getName()).append(")");
			message.append("\n");
			message.append("Result: ").append(deployment.getDeploymentState().toString());
			message.append("\n");
			message.append(deployment.getStateMessage());
			message.append("\n");
			message.append("\n");
		}
		return message.toString();
	}

	/**
	 * Extracts the receipients interested in getting notifications about the
	 * deployment execution.
	 * 
	 * @param deployments
	 * @return - an array of e-mail addresses
	 * @throws AddressException
	 */
	private Address[] getAllReceipients(final List<DeploymentEntity> deployments)
			throws AddressException {

		final Set<String> emailReceipients = new HashSet<String>();
		final Set<Integer> groupIds = new HashSet<Integer>();

		for (final DeploymentEntity deployment : deployments) {
			if (deployment.isSendEmail()) {
				emailReceipients.add(deployment.getDeploymentRequestUser());
			}
			if (deployment.isSendEmailConfirmation()) {
				emailReceipients
				.add(deployment.getDeploymentConfirmationUser());
			}
			ReleaseEntity release = deployment.getRelease();
			if(release == null){
				// get Past Release
				release = releaseMgmtService.getDefaultRelease();

			}
			// Find the resourceGroup ids for the deployment
			final ResourceEntity resource = resourceDependencyResolverService.getResourceEntityForRelease(deployment.getResourceGroup(),
					release);
			if (resource != null) {
				groupIds.add(resource.getResourceGroup().getId());
				if (resource.getConsumedMasterRelations() != null) {
					for (final ConsumedResourceRelationEntity rel : resourceDependencyResolverService
							.getConsumedMasterRelationsForRelease(resource,
									deployment.getRelease())) {
						groupIds.add(rel.getSlaveResource().getResourceGroup().getId());
					}
				}
			}
		}

		List<String> userNames = userSettingsService
				.getRegisteredUsernamesForResourcesIds(groupIds);
		emailReceipients.addAll(userNames);

		final Address[] to = new InternetAddress[emailReceipients.size()];
		int i = 0;
		for (final String user : emailReceipients) {
			// TODO make configurable
			to[i++] = new InternetAddress(user + "@"
					+ ConfigurationService.getProperty(ConfigKey.MAIL_DOMAIN));
		}

		return to;
	}

	/**
	 * Generates a failure message for the unsuccessful sending of a
	 * notification email.
	 * 
	 * @param e
	 * @return
	 */
	private String getFailureSendMailMessage(final String e) {
		return "Failure occoured while sending notification email: " + e;
	}

	/**
	 * Generates a success message for email sending process.
	 * 
	 * @param emailReceipients
	 *            - the receipients to which the email was successfully sent
	 * @return
	 */
	private String getSuccessfulSendMailToReceipientMessage(
			final Address[] emailReceipients) {
		final StringBuffer result = new StringBuffer(
				"Notification email sent to following receipients: \n");
		for (final Address address : emailReceipients) {
			result.append(address.toString());
			result.append("\n");
		}
		return result.toString();
	}
}
