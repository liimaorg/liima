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

package ch.puzzle.itc.mobiliar.presentation.generator;

import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;

public class ComparedGeneratedTemplates implements Comparable<ComparedGeneratedTemplates>{

	@Getter
	private final String path;
	@Getter
	@Setter
	private GeneratedTemplate originalTemplate;
	@Getter
	@Setter
	private GeneratedTemplate comparedTemplate;
	
	public ComparedGeneratedTemplates(String path) {
		super();
		this.path = path;		
	}



     public boolean isOmitted(){
	    return (originalTemplate!=null && originalTemplate.isOmitted()) || (comparedTemplate!=null && comparedTemplate.isOmitted());
	}

	public boolean hasOriginalTemplate(){
		return originalTemplate!=null;
	}
	
	public boolean hasComparedTemplate(){
		return comparedTemplate!=null;
	}
	
	public boolean sameContent(){
		if(originalTemplate==null || originalTemplate.getContent()==null){
			return comparedTemplate==null || comparedTemplate.getContent()==null;
		}
		else{
		    	return originalTemplate.isSameContent(comparedTemplate);
		}
	}
	
	@Override
	public int compareTo(ComparedGeneratedTemplates o) {
		if(o==null){
			return 1;
		}
		else if(path==null){
			return o.getPath()==null ? 0 : -1;
		}
		else if(o.getPath()==null){
			return 1;
		}
		else{
			return path.compareTo(o.getPath());
		}
	}
	
	
}
