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

package ch.puzzle.itc.mobiliar.business.security.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TAMW_permission")
public class PermissionEntity implements Serializable
{

	private static final long serialVersionUID = 1L;
	@Getter
	@Setter
	@TableGenerator(name = "permissionIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, 
			valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "permissionId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "permissionIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Setter
	@Column(unique = true)
	private String value;

	@Getter
	@Version
	private long v;

	@Getter
	@Setter
	@ManyToMany(mappedBy = "permissions")
	@NotAudited
	private Set<RoleEntity> roles = new HashSet<RoleEntity>();

	@Getter
	@Setter
	@OneToMany(mappedBy = "permission")
	private Set<RestrictionEntity> restrictions = new HashSet<>();

	public String getInfo() {
		try {
			Permission p = Permission.valueOf(getValue());
			return p.getInfo();
		} catch (Exception e) {
			return "";
		}
	}


}
