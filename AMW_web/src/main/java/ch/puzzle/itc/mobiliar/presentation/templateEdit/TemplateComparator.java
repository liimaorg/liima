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

package ch.puzzle.itc.mobiliar.presentation.templateEdit;

import java.util.Comparator;

import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

public class TemplateComparator implements Comparator<TemplateDescriptorEntity>{

	@Override
	public int compare(TemplateDescriptorEntity arg0, TemplateDescriptorEntity arg1) {
		if(arg0== null && arg1 == null 
				|| arg0.getName()== null && arg1.getName() == null){
			return 0;
		}else if(arg0== null || arg0.getName() == null){
			return -1;
		}
		else if(arg1== null || arg1.getName() == null){
			return 1;
		}else{
			// compare by Name
			return arg0.getName().compareTo(arg1.getName());
		}
	}
}
