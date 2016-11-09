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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO für JavaBatch Monitor: Job Inventar
 * @author U110565
 *
 */
@XmlRootElement(name = "batchJobInventory")
@XmlAccessorType(XmlAccessType.FIELD)
public class BatchJobInventoryDTO {

    private List<ResourceDTO> resources = new ArrayList<ResourceDTO>();

    private List<BatchResourceRelationDTO> batchRelations = new ArrayList<BatchResourceRelationDTO>();

    private List<String> warnings = new ArrayList<>();
       
    public BatchJobInventoryDTO(){}

    public BatchJobInventoryDTO(List<ResourceDTO> resources, List<BatchResourceRelationDTO> relations){
        if(resources!=null && !resources.isEmpty()){
            this.resources = resources;
        }
        
        if(relations!=null && !relations.isEmpty()){
            this.batchRelations = relations;
        }
    }

    public List<ResourceDTO> getResources() {
        return resources;
    }

    public void setResources(List<ResourceDTO> resources) {
        this.resources = resources;
    }

    public List<BatchResourceRelationDTO> getBatchJobs() {
        return batchRelations;
    }

    public void setRelations(List<BatchResourceRelationDTO> relations) {
        this.batchRelations = relations;
    }

    public List<String> getWarnings() {
        return warnings;
    }

}
