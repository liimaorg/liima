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

package ch.puzzle.itc.mobiliar.business.template.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

@Getter
@AllArgsConstructor
public class RevisionInformation implements Comparable<RevisionInformation>{

	private final Number revision;
	private final Date revisionDate;
	private final String userName;
	
	
	public String getDisplayName(){
		return DateFormatUtils.format(revisionDate, "dd.MM.yyyy HH:mm:ss")+" ("+userName+")";
	}


	@Override
	public int compareTo(RevisionInformation o) {
		if(o==null){
			return 1;
		}
		if(revisionDate==null){
			return o.getRevisionDate()==null ? 0 : 1;
		}
		return revisionDate.compareTo(o.getRevisionDate());
	}
}
