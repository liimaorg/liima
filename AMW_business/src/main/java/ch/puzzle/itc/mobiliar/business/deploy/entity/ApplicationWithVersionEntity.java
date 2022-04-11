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

package ch.puzzle.itc.mobiliar.business.deploy.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: ApplicationWithVersion
 * Replaced the old json structure and should be preserved when an app is deleted.
 */
@Entity
@Table(name = "TAMW_APPLICATIONWITHVERSION")
public class ApplicationWithVersionEntity implements Serializable {


    @Getter
    @Setter
    @TableGenerator(name = "applicationWithVersionIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "applicationWithVersionId")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "applicationWithVersionIdGen")
    @Id
    @Column(unique = true, nullable = false)
    private Integer id;

    @Getter
    @Setter
    @ManyToOne
    private DeploymentEntity deployment;

    @Getter
    @Setter
    @ManyToOne
    private ResourceEntity application;

    @Getter
    @Setter
    @Column(name = "ex_context_id")
    private Integer exApplicationId;

    @Getter
    @Setter
    private String version;

    public ApplicationWithVersionEntity() {}

    public ApplicationWithVersionEntity(ResourceEntity application, String version) {
        this.application = application;
        this.version = version;
    }

    public ApplicationWithVersionEntity(DeploymentEntity deployment, ResourceEntity application, String version) {
        this(application, version);
        this.deployment = deployment;
    }

    @Override
    public String toString() {
        return "ApplicationWithVersion [applicationName=" + application.getName() + ", applicationId=" + application.getId()
                + ", version=" + version + "]";
    }


}
