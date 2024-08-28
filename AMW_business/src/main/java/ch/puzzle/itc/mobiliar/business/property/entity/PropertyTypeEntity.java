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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Audited
//Dieses Query wird gebraucht nur um ein Beispiel zu testen
@NamedQuery(name = "propertiesNames", query = "select p from PropertyTypeEntity p",  cacheable = true)
@Table(name="TAMW_propertyType")
public class PropertyTypeEntity implements PropertyTagEntityHolder {
	
	@Getter
	@Setter
	@TableGenerator(name = "propertytypeIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "propertytypeId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "propertytypeIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;
	
	@Getter
	@Setter
	private String validationRegex;

	@Setter
	@Getter
	@Column(nullable = false)
	private boolean encrypt;

	@Setter
	@ManyToMany
	@JoinTable(name = "TAMW_propType_propTag", joinColumns = { @JoinColumn(name = "PROPERTYTYPE_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "PROPERTYTAG_ID", referencedColumnName = "ID") })
	private List<PropertyTagEntity> propertyTags;
	
	@Getter
	@Setter
	private String propertyTypeName;
	
	@Getter
	@Version
	private long v;
	
	//For cascading only
	@Getter
	@Setter
	@OneToMany(mappedBy = "propertyTypeEntity")
	private Set<PropertyDescriptorEntity> propertyDescriptors;

	public List<PropertyTagEntity> getPropertyTags() {
		return propertyTags == null ? new ArrayList<PropertyTagEntity>() : propertyTags;
	}

	@Override
	public Set<String> getPropertyTagsNameSet() {
		Set<String> tagNames = new HashSet<>();
		for (PropertyTagEntity propertyTagEntity : getPropertyTags()) {
			tagNames.add(propertyTagEntity.getName());
		}
		return tagNames;
	}

    @Override
    public void addPropertyTag(PropertyTagEntity propertyTagEntity) {
        if(propertyTags == null){
            propertyTags = new ArrayList<>();
        }
		if (!hasTagWithName(propertyTagEntity)) {
			propertyTags.add(propertyTagEntity);
		}
    }

	private boolean hasTagWithName(PropertyTagEntity newTagEntity) {
		for(PropertyTagEntity tag : propertyTags){
			if (newTagEntity != null && newTagEntity.getName().equals(tag.getName())){
				return true;
			}
		}
		return false;
	}

    @Override
    public void removePropertyTag(PropertyTagEntity propertyTagEntity) {
        if(propertyTags != null){
            propertyTags.remove(propertyTagEntity);
        }
    }
}
