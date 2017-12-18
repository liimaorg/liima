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

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import javax.persistence.*;
import java.util.Set;

@Entity
@RevisionEntity(MyRevisionEntityListener.class)
@Table(name = "TAMW_revinfo")
public class MyRevisionEntity {

	@Getter
	@Setter
	@TableGenerator(name = "revinfoIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "revinfoId", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "revinfoIdGen")
	@Id
	@Column(unique = true, nullable = false)
	@RevisionNumber
	private int id;

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	@RevisionTimestamp
	private long timestamp;
	
	@Getter
	@Version
	private long v;

	@ElementCollection
	@JoinTable(name = "TAMW_REVCHANGES", joinColumns = @JoinColumn(name = "REV"))
	@Column(name = "ENTITYNAME")
	@ModifiedEntityNames
	private Set<String> modifiedEntityNames;

}
