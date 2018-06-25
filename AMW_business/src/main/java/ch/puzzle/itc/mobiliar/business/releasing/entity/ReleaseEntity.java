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

package ch.puzzle.itc.mobiliar.business.releasing.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The releaseEntity defines a specific release unit and holds the relations to
 * the different resources which are explicitly defined for the release.
 * 
 */
@XmlRootElement(name="release")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Audited
@Table(name = "TAMW_release")
@EqualsAndHashCode(exclude={"resources", "deployments", "outOfServiceResourceGroups"})
@JsonIgnoreProperties(value= {"resources", "deployments", "outOfServiceResourceGroups"})
public class ReleaseEntity implements Serializable, Comparable<ReleaseEntity> {

	@Getter
	@Setter
	@TableGenerator(name = "releaseIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "releaseId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "releaseIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	/**
	 * The name of the release
	 */
	@Getter
	@Setter
	private String name;
	/**
	 * Additional information about the description for better readability -
	 * e.g. "Oktober Release 2013"
	 */
	@Getter
	@Setter
	private String description;

	/**
	 * Defines the date when this release is applied in production environment.
	 * This value is used for the comparison / definition of the order of
	 * releases.
	 */
	@Getter
	@Setter
	@Column(name = "INSTALLATIONINPRODUCTION")
	@Temporal(TemporalType.DATE)
	private Date installationInProductionAt;

	/**
	 * The resource entities which are defined for this release.
	 */
	@Setter
	@Getter
	@OneToMany(mappedBy = "release")
	@XmlTransient
	@JsonIgnore
	private Set<ResourceEntity> resources;
	

	/**
	 * The deployments defined for this release - please note, that this
	 * reference only exists to control cascading and for completeness - there
	 * is no use case (yet) in which this direction of the relation might be
	 * useful
	 */
	@Getter
	@Setter
	@OneToMany(mappedBy = "release")
	@NotAudited
	@XmlTransient
	@JsonIgnore
	private Set<DeploymentEntity> deployments;

	/**
	 * The resource groups which are "out of service" with this release. Please
	 * note that this reference only exists to control cascading and for
	 * completeness - there is no use case (yet) in which this direction of the
	 * relation might be useful
	 */
	@Getter
	@Setter
	@OneToMany(mappedBy = "outOfServiceRelease")
	@XmlTransient
	@JsonIgnore
	private Set<ResourceGroupEntity> outOfServiceResourceGroups;

	@Getter
	@Setter
	@Column(nullable = false, name = "MAINRELEASE")
	private boolean mainRelease;

	@XmlTransient
	@JsonIgnore
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@Version
	@XmlTransient
	@JsonIgnore
	private long v;

	public ReleaseEntity() {
		super();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(ReleaseEntity o) {
		if (o == null || o.getInstallationInProductionAt() == null) {
			return this.getInstallationInProductionAt() == null ? 0 : -1;
		}
		return this.getInstallationInProductionAt() == null ? 1 : this.getInstallationInProductionAt().compareTo(o.getInstallationInProductionAt());
	}
}
