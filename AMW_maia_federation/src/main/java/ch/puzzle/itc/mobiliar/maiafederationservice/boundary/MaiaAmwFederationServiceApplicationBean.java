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

package ch.puzzle.itc.mobiliar.maiafederationservice.boundary;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.Application;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederation.v1_0.ApplicationID;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.*;
import ch.mobi.xml.datatype.common.commons.v3.CallContext;
import ch.mobi.xml.datatype.common.commons.v3.MessageSeverity;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.predecessor.boundary.MaiaAmwFederationServicePredecessorHandler;
import ch.puzzle.itc.mobiliar.business.predecessor.entity.PredecessorResult;
import ch.puzzle.itc.mobiliar.business.predecessor.entity.PredecessorResultMessage;
import ch.puzzle.itc.mobiliar.business.predecessor.entity.ProcessingState;
import ch.puzzle.itc.mobiliar.common.exception.AMWRuntimeException;
import ch.puzzle.itc.mobiliar.maiafederationservice.entity.ResourceHelper;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState.FAILED;
import static ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.ProcessingState.OK;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MaiaAmwFederationServiceApplicationBean  {

    @Inject
    private MaiaAmwFederationServiceImportHandler federationServiceImportHandler;

    @Inject
    private MaiaAmwFederationServicePredecessorHandler federationServicePredecessorHandler;

    @Inject
    private MaiaAmwFederationServiceRemovalHandler federationServiceRemovalHandler;

    @Inject
    private Logger log;

    public UpdateResponse update(CallContext callContext, String ownerString, UpdateRequest updateRequest) throws ValidationException, BusinessException, TechnicalException {

        Objects.requireNonNull(callContext, "CallContext must not be null!");
        Objects.requireNonNull(ownerString, "Owner must not be null!");
        Objects.requireNonNull(updateRequest, "UpdateRequest must not be null!");

        // TODO Schnittstelle: maybe to some verification if caller owner matches some criteria ?
        log.info("Start federation service import by " + callContext.toString());

        List<ApplicationUpdateResult> processedApplicationsResponse = new ArrayList<>();
        List<String> importedApps = new ArrayList<>();

        List<Application> apps = updateRequest.getApplications();
        for (Application app : apps) {
            ResourceHelper resourceHelper = handleUpdateApp(app);
            processedApplicationsResponse.add(new ApplicationUpdateResult(app.getId(), resourceHelper.getProcessingState(), federationServiceImportHandler.buildAmwResourceLink(app.getId().getName()),resourceHelper.getMessages()));

            // only add newly created apps
            if (resourceHelper.isNouveau()) {
                importedApps.add(app.getId().getName());
            }
        }

        log.info("End federation service import by " + callContext.toString());

        log.info("Start federation service predecessor handling by "+callContext.toString());

        for (ApplicationPredecessorRelation appPredecessor : updateRequest.getApplicationPredecessors()) {
            ResourceHelper resourceHelper = handlePredecessor(appPredecessor, importedApps);

            String appName = resourceHelper.getAppName();
            for (ApplicationUpdateResult updateResult : processedApplicationsResponse) {
                if (updateResult.getId().getName().equals(appName)) {
                    // add messages
                    updateResult.getMessages().addAll(resourceHelper.getMessages());
                    // set the correct state
                    if (resourceHelper.getProcessingState() != null) {
                        updateResult.setState(resourceHelper.getProcessingState());
                    }
                }
            }
        }

        log.info("End federation service predecessor handling by "+callContext.toString());

        log.info("Start federation service removal handling by "+callContext.toString());

        List<ApplicationID> appsToBeDeleted = updateRequest.getRemovedApplications();
        for (ApplicationID appToBeDeleted : appsToBeDeleted) {
            ResourceHelper resourceHelper = federationServiceRemovalHandler.handleRemoval(appToBeDeleted.getName());
            processedApplicationsResponse.add(new ApplicationUpdateResult(appToBeDeleted, resourceHelper.getProcessingState(), "",resourceHelper.getMessages()));
        }

        log.info("End federation service removal handling by "+callContext.toString());

        return new UpdateResponse(processedApplicationsResponse);
    }

    private ResourceHelper handleUpdateApp(Application app) {
        log.info("Start importing app " + app.toString());
        try {
            return federationServiceImportHandler.handleUpdateAggregate(app);
        }
        catch(AMWRuntimeException e) {
            log.warning("Exception occurred while importing aggregate " + app.toString() + " Reason: " + e.getMessage());
            return handleException(app.getId().getName(),  "Creation of application " + app.getId().getName() + " failed. Reason: " + e.getMessage());
        }
        catch(EJBException e) {
            if (e.getCause() instanceof AMWRuntimeException) {
                log.warning("Exception occurred while importing aggregate " + app.toString() + " Reason: " + e.getMessage());

                return handleException(app.getId().getName(),  "Creation of application " + app.getId().getName() + " failed. Reason: "+e.getMessage());
            }
            // handleUpdateAggregate throw other than expected exception
            log.warning("Unexpected exception occurred while importing aggregate "+app.toString()+" Reason: "+e.getMessage());
            throw e;
        }
        finally {
            log.info("End importing app "+app.toString());
        }
    }

    private ResourceHelper handlePredecessor(ApplicationPredecessorRelation appPredecessor, List<String> importedApps) {
        log.info("Start importing appPredecessor "+appPredecessor.toString());
        final String successorName = appPredecessor.getNewApplication().getName();
        final String predecessorName = appPredecessor.getPredecessorApplication().getName();
        try{
            // check if Successor was created during this import
            if (!importedApps.contains(successorName)) {
                throw new AMWRuntimeException("New application "+successorName+" was not imported during this request");
            }
            PredecessorResult predecessorResult = federationServicePredecessorHandler.handlePredecessor(successorName, predecessorName, ForeignableOwner.MAIA);
            return convertResult(predecessorResult);
        }
        catch (AMWRuntimeException e ) {
            log.warning("Exception occurred while importing appPredecessor " + appPredecessor.toString() + " Reason: " + e.getMessage());
            return handleException(appPredecessor.getNewApplication().getName(),  e.getMessage());
        }
        catch(EJBException e) {
            if (e.getCause() instanceof AMWRuntimeException) {
                log.warning("Exception occurred while importing appPredecessor " + appPredecessor.toString() + " Reason: " + e.getMessage());
                return handleException( appPredecessor.getNewApplication().getName(), e.getMessage());
            }
            // handlePredecessor throw other than expected exception
            log.warning("Unexpected exception occurred while importing aggregate "+appPredecessor.toString()+" Reason: "+e.getMessage());
            throw e;
        }
        finally {
            log.info("End importing appPredecessor " + appPredecessor.toString());
        }
    }

    private ResourceHelper handleException(String applicationName, String errorMessage) {
        ResourceHelper resourceHelper = new ResourceHelper();
        resourceHelper.setAppName(applicationName);
        resourceHelper.addMessage(new Message(MessageSeverity.ERROR, errorMessage));
        resourceHelper.setProcessingState(FAILED);
        resourceHelper.setAppLink(federationServiceImportHandler.buildAmwResourceLink(applicationName));

        return resourceHelper;
    }

    private ResourceHelper convertResult(PredecessorResult predecessorResult){
        ResourceHelper resourceHelper = new ResourceHelper();
        resourceHelper.setAppName(predecessorResult.getAppName());
        resourceHelper.setAppLink(federationServiceImportHandler.buildAmwResourceLink(predecessorResult.getAppName()));
        if(ProcessingState.OK.equals(predecessorResult.getProcessingState())){
            resourceHelper.setProcessingState(OK);
        }else{
            resourceHelper.setProcessingState(FAILED);
        }
        for (PredecessorResultMessage predecessorResultMessage : predecessorResult.getMessages()) {
            MessageSeverity severity = MessageSeverity.INFO;
            if(MessageSeverity.ERROR.value().equals(predecessorResultMessage.getSeverity().value())){
                severity = MessageSeverity.ERROR;
            }else if(MessageSeverity.WARNING.value().equals(predecessorResultMessage.getSeverity().value())){
                severity = MessageSeverity.WARNING;
            }
            resourceHelper.addMessage(new Message(severity, predecessorResultMessage.getHumanReadableMessage()));
        }
        return resourceHelper;
    }

    public void ping() {
        // TODO implement ?
    }
}
