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

import java.util.ArrayList;

import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.utils.notification.NotificationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigKey;

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
	private ResourceDependencyResolverService resourceDependencyResolverService;

	@Inject
	private Logger log;

	/**
	 * Creates a notification email for deployment executions and sends them to
	 * the given recipients
	 *
	 * @param deployments
	 *            - the executed deployments
	 * @return the message to be stored with the deployment.
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public String createAndSendMailForDeplyoments(final List<DeploymentEntity> deployments) {
		String message = null;
		// do nothing if no deployment is available
		if (deployments == null || deployments.isEmpty()) {
			return message;
		}
		String subjectMessage = "Liima-Deploy for tracking id: " + deployments.get(0).getTrackingId();
		List<DeploymentEntity> filteredDeployments = new ArrayList<>();
		for (DeploymentEntity deployment : deployments) {
			// skip notification if deployment is preserved. Can happen if old deployments are stuck in progress that the scheduler tries to clean up.
			if (deployment.isPreserved()) {
				log.log(Level.INFO, "Deployment notification for deployment {0} will not be sent because it's preserved", deployment.getId());
				continue;
			}
			filteredDeployments.add(deployment);
		}
		if (filteredDeployments.isEmpty()) {
			return message;
		}

		try {
			Address[] emailRecipients = getAllRecipients(filteredDeployments);
			if (notificationService.createAndSendMail(subjectMessage, getMessageContentForDeployments(filteredDeployments), emailRecipients)) {
				message = getSuccessfulSendMailToRecipientMessage(emailRecipients);
			}
		} catch (MessagingException e) {
			message = getFailureSendMailMessage(e.getMessage());
			log.log(Level.WARNING, "Deployment notification (" + subjectMessage + ") could not be sent", e);
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
	private String getMessageContentForDeployments(final List<DeploymentEntity> deployments) {
		final StringBuffer message = new StringBuffer();
		for (final DeploymentEntity deployment : deployments) {
			message.append("Application server: ").append(deployment.getResourceGroup().getName());
			message.append(" (").append(deployment.getRelease()).append(")\n");
			message.append("Applications: \n");
			message.append(getApplicationWithVersionsString(deployment));
			message.append("Result: ").append(deployment.getDeploymentState().toString()).append("\n");
			message.append("Environment: ").append(deployment.getContext().getName()).append("\n");
			message.append("Id: ").append(deployment.getId()).append("\n");
			message.append("\n");
			message.append("State message:\n");
			message.append(deployment.getStateMessage()).append("\n");
			message.append("\n\n");
		}
		return message.toString();
	}

	private String getApplicationWithVersionsString(DeploymentEntity deploymentEntity){
		StringBuffer result = new StringBuffer();
		if(deploymentEntity != null){
			List<DeploymentEntity.ApplicationWithVersion> applicationsWithVersions = deploymentEntity.getApplicationsWithVersion();
			for (DeploymentEntity.ApplicationWithVersion applicationsWithVersion : applicationsWithVersions ){
				result.append("- ");
				result.append(applicationsWithVersion.getApplicationName());
				result.append(" ");
				result.append(applicationsWithVersion.getVersion());
				result.append("\n");
			}
		}
		return result.toString();
	}

	/**
	 * Extracts the recipients interested in getting notifications about the
	 * deployment execution.
	 * 
	 * @param deployments
	 * @return - an array of e-mail addresses
	 * @throws AddressException
	 */
	private Address[] getAllRecipients(final List<DeploymentEntity> deployments)
			throws AddressException {

		final Set<String> emailRecipients = new HashSet<String>();
		final Set<Integer> groupIds = new HashSet<Integer>();

		for (final DeploymentEntity deployment : deployments) {
			if (deployment.isSendEmail()) {
				emailRecipients.add(deployment.getDeploymentRequestUser());
			}
			if (deployment.isSendEmailConfirmation()) {
				emailRecipients.add(deployment.getDeploymentConfirmationUser());
			}
			groupIds.add(deployment.getResourceGroup().getId());
			if (deployment.getResource().getConsumedMasterRelations() == null) {
				continue;
			}
			for (final ConsumedResourceRelationEntity rel : resourceDependencyResolverService.getConsumedMasterRelationsForRelease(deployment.getResource(), deployment.getRelease())) {
				groupIds.add(rel.getSlaveResource().getResourceGroup().getId());
			}
		}

		final Address[] to = new InternetAddress[emailRecipients.size()];
		int i = 0;
		for (final String user : emailRecipients) {
			to[i++] = new InternetAddress(user + "@" + ConfigurationService.getProperty(ConfigKey.MAIL_DOMAIN));
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
	 * @param emailRecipients
	 *            - the recipients to which the email was successfully sent
	 * @return
	 */
	private String getSuccessfulSendMailToRecipientMessage(
			final Address[] emailRecipients) {
		final StringBuffer result = new StringBuffer(
				"Notification email sent to following recipients: \n");
		for (final Address address : emailRecipients) {
			result.append(address.toString());
			result.append("\n");
		}
		return result.toString();
	}
}
