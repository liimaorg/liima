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

package ch.puzzle.itc.mobiliar.presentation.propertyEdit;

import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwnerViolationException;
import ch.puzzle.itc.mobiliar.business.utils.ValidationException;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.util.NameChecker;
import ch.puzzle.itc.mobiliar.presentation.resourceRelation.ResourceRelationModel;
import ch.puzzle.itc.mobiliar.presentation.resourcesedit.EditResourceView;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import org.apache.commons.lang.StringUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class PropertyEditController {

	@Inject
	Logger log;

	@Inject
	PropertyEditDataProvider propertyDataProvider;

	@Inject
	ResourceRelationModel resourceRelation;

	@Inject
	EditResourceView resource;

	public void save() {
		String message;
	     if(resource.isEditResource() && !NameChecker.isNameValid(resource.getResource().getName())) {
			   GlobalMessageAppender
					   .addErrorMessage(NameChecker.getErrorTextForResourceType(resource
                               .getResourceType().getName(), resource.getResource().getName()));
		}
		else {
			message = "Changes successfully saved.";
			try {
				 propertyDataProvider.save();
				 GlobalMessageAppender.addSuccessMessage(message);
			}
            catch (ForeignableOwnerViolationException e) {
                String errorMessage = "Edit resource not allowed by owner "+e.getViolatingOwner();
                GlobalMessageAppender.addErrorMessage(errorMessage);
                log.log(Level.SEVERE, errorMessage, e);
            }
			catch (AMWException | ValidationException e) {
				GlobalMessageAppender.addErrorMessage(e.getMessage());
				log.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

    private String getRefreshOutcomeWithRelation(String relationId, String ctx, String resourceId, String resourceTypeId) {
        StringBuilder urlBuilder = new StringBuilder(FacesContext.getCurrentInstance().getViewRoot().getViewId());
        urlBuilder.append("?faces-redirect=true")
                .append(buildParamIfNotNull(EditResourceView.RESOURCE_ID, resourceId))
                .append(buildParamIfNotNull(EditResourceView.CONTEXT_ID, ctx))
                .append(buildParamIfNotNull(EditResourceView.RESOURCE_TYPE_ID, resourceTypeId))
                .append(buildParamIfNotNull(EditResourceView.RELATION_ID, relationId));
        return urlBuilder.toString();
    }

    private String buildParamIfNotNull(String key, String value) {
        return StringUtils.isNotEmpty(value) ? String.format("&%s=%s", key, value) : StringUtils.EMPTY;
    }

}
