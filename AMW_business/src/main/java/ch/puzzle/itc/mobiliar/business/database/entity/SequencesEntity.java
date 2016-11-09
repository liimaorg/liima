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

package ch.puzzle.itc.mobiliar.business.database.entity;

import ch.puzzle.itc.mobiliar.business.database.control.SequencesService;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Entity implementation for the SAMW_SEQUENCES table.<br>
 * <b>Do not use this entity outside from {@link SequencesService} !</b>
 * 
 * @author cweber
 */
@Entity
@Table(name = "SAMW_sequences")
public class SequencesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	@Id
	@Column(name = "SEQ_NAME", unique = true, nullable = false)
	private String sequenceName;

	@Getter
	@Setter
	@Column(name = "NEXT_VAL", unique = false, nullable = false)
	private Integer nextValue;

}
