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

import java.io.Serializable;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.envers.Audited;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;

/**
 * Entity implementation class for Entity: PropertyTag
 */
@Entity
@Audited
@Table(name = "TAMW_propertyTag")
public class PropertyTagEntity implements Identifiable, Serializable {

    @Getter
    @Setter
    @TableGenerator(name = "propertyTagIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "propertyTagId")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "propertyTagIdGen")
    @Id
    @Column(unique = true, nullable = false)
    private Integer id;

    @Getter
    @Column(nullable = false)
    private String name;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private PropertyTagType tagType;

    public void setName(String name) {
        if (name != null) {
            this.name = name.trim();
        }
    }

    @Override
    public String toString() {
        return "Tag [id=" + id + ", name=" + name + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }

        if ( !(obj instanceof PropertyTagEntity) ){
            return false;
        }
        PropertyTagEntity that = (PropertyTagEntity)obj;

        return new EqualsBuilder().append(this.id, that.id)
                .append(this.name, that.name)
                .append(this.tagType, that.tagType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.name)
                .append(this.tagType)
                .toHashCode();
    }
}