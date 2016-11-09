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

package ch.puzzle.itc.mobiliar.business.property.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.utils.ValidationHelper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Mik (Machine Interpretation Key)
 */
@Entity
@Audited
@Table(name = "TAMW_mik")
public class MikEntity {

	/**
	 * Default constructor to make the jpa happy :-)
	 */
	protected MikEntity() {}

	public MikEntity(String name, AmwFunctionEntity amwFunction) {
		String validName = ValidationHelper.validateNotNullOrEmpty(name).trim();
		if (!validName.isEmpty()) {
			this.name = validName;
		}
        this.amwFunction = amwFunction;
	}

	@Getter
	@Setter
	@TableGenerator(name = "mikIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "mikId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "mikIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Column(nullable = false)
	private String name;

	@Getter
	@ManyToOne
	private AmwFunctionEntity amwFunction;

	@Override
	public String toString() {
		return "Mik [id=" + id + ", name=" + name + "]";
	}

}
