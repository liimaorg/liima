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

package ch.mobi.itc.mobiliar.rest.dtos;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.ResourceGroupLocator;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashSet;

@ApplicationScoped
public class TemplateMapper {

    @Inject
    private ResourceGroupLocator resourceGroupLocator;

    public TemplateDescriptorEntity toTemplateDescriptorEntity(TemplateDTO templateDTO, TemplateDescriptorEntity template) {
        if (template == null) {
            template = new TemplateDescriptorEntity();
        }
        template.setId(templateDTO.getId());
        template.setFileContent(templateDTO.getFileContent());
        template.setName(templateDTO.getName());
        template.setTargetPath(templateDTO.getTargetPath());
        HashSet<ResourceGroupEntity> targetPlatforms = new HashSet<>();
        if (templateDTO.getTargetPlatforms() != null) {
            for (String platform : templateDTO.getTargetPlatforms()) {
                ResourceGroupEntity platformEntity = resourceGroupLocator.getResourceGroupByName(platform);
                targetPlatforms.add(platformEntity);
            }
        }
        template.setTargetPlatforms(targetPlatforms);
        if (templateDTO.getVersion() != null) {
            template.setV(templateDTO.getVersion());
        }

        return template;
    }
}
