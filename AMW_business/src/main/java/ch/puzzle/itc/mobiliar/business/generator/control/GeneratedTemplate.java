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

package ch.puzzle.itc.mobiliar.business.generator.control;

import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;
import ch.puzzle.itc.mobiliar.common.exception.TemplatePropertyException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratedTemplate {
	
	public static String RESERVED_PROPERTY_NAME = "name";
	public static String RESERVED_PROPERTY_PATH = "path";
	public static String RESERVED_PROPERTY_CONTENT = "content";
	public static String RESERVED_PROPERTY_IS_RELATION_TEMPLATE = "isRelationTemplate";
	
	@Getter
	private String name;
	@Getter
	private String path;

	private String content;
	@Getter
	@Setter
	private boolean omitted;

	@Getter
	@Setter
	private TemplateDescriptorEntity templateEntity;
	
	@Getter
	private List<TemplatePropertyException> errorMessages = new ArrayList<TemplatePropertyException>();

	public String getContent(){
	    if(isOmitted()){
		   return content==null ? null : "content has been omitted.";
	    }
	    return content;
	}

	public GeneratedTemplate(final String name, final String path, final String content) {
		super();
		this.name = name;
		this.path = path;
		this.content = content;
	}

	public Map<String, String> toHash() {
		Map<String, String> hash = new HashMap<>();
		hash.put(RESERVED_PROPERTY_PATH, path);
		hash.put(RESERVED_PROPERTY_CONTENT, content);
		hash.put(RESERVED_PROPERTY_NAME, name);
		return hash;
	}
	
	@Override
	public String toString() {
		return "GeneratedTemplate [name=" + name + ", path=" + path + ", content=" + content.length() + "]";
	}

	/**
	 * Adds all ErrorMessages
	 * 
	 * @param errorMessages
	 */
	public void addAllErrorMessages(List<TemplatePropertyException> errorMessages) {
		this.errorMessages.addAll(errorMessages);
	}
	
	/**
	 * Generates the ErrorMessages as a String
	 * @return
	 */
	public String getErrorMessageAsString() {
		StringBuilder sb = new StringBuilder();
		for (TemplatePropertyException e : errorMessages) {
			sb.append("Template " + name+ " ("+path+"): " + e.getMessage() + "\n");
		}
		return sb.toString();
	}
	
	public boolean hasErrors(){
		if(errorMessages != null && !errorMessages.isEmpty()){
			return true;
		}
		return false;
	}

     public boolean isSameContent(GeneratedTemplate otherTemplate){
	    if(otherTemplate==null){
		   return false;
	    }
	    return content.equals(otherTemplate.content);
	}	
}
