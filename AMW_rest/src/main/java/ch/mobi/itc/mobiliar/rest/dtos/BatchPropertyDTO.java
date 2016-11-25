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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO für JavaBatch Monitor: Job Properties
 * @author U110565
 *
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class BatchPropertyDTO{

    @Getter @Setter
    private String batchApp;   
    @Getter @Setter
    private String jobId;
    @Getter @Setter
    private List<PropertyDTO> properties;

    BatchPropertyDTO(){}

    public BatchPropertyDTO(String batchApp, String jobId, List<PropertyDTO> properties){
        this.batchApp=batchApp;
        this.jobId=jobId;
        this.properties=properties;

    }
}
