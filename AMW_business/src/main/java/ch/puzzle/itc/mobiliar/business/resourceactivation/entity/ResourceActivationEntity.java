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

package ch.puzzle.itc.mobiliar.business.resourceactivation.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ResourceRelationContextEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Audited
@Table(name = "TAMW_resourceActivation")
public class ResourceActivationEntity implements Serializable {

	@TableGenerator(name = "resActIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "resActId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "resActIdGen")
	@Id
	@Column(unique = true, nullable = false)
	@Getter
	private Integer id;

	@Getter
	@Setter
	@ManyToOne
	@JoinColumn(name = "resRelCtx_ID")
	ResourceRelationContextEntity resourceRelationContext;

	@ManyToOne
	@JoinColumn(name = "resGroup_ID")
	@Getter
	@Setter
	ResourceGroupEntity resourceGroup;

	@Getter
	@Setter
	boolean active;

	@Getter
	@Version
	private long v;

	@Transient
	@Getter
	@Setter
	boolean onlyActivationEntityForResourceRelation;
}
