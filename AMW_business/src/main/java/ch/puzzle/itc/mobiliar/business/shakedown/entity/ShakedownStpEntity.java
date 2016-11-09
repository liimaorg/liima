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

package ch.puzzle.itc.mobiliar.business.shakedown.entity;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: ShakedownStp
 * 
 */
@Entity
@Audited
@Table(name = "TAMW_shakedownStp")
public class ShakedownStpEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@TableGenerator(name = "shakedownStpIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "shakedownStpId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "shakedownStpIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Setter
	@Column(unique = true, nullable = false)
	private String stpName;

	@Getter
	@Setter
	private String version;

	@Getter
	@Setter
	private String comaSeperatedParameters;

	@Getter
	@Version
	private long v;

	public ShakedownStpEntity(){
		comaSeperatedParameters = "";
	}



	/**
	 * adds a Parameter to the list
	 * 
	 * @param parameterName
	 * @return true if parameterName was added, false if parameterName already exists
	 */
	public boolean addParameter(String parameterName) {
		if (comaSeperatedParameters==null || comaSeperatedParameters.isEmpty()) {
			comaSeperatedParameters = parameterName;
			return true;
		} else if (!comaSeperatedParameters.contains(parameterName)) {
			comaSeperatedParameters += "," + parameterName;
			return true;
		}
		return false;
	}

}
