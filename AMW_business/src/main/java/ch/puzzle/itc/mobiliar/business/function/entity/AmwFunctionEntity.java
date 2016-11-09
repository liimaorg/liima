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

package ch.puzzle.itc.mobiliar.business.function.entity;

import static javax.persistence.CascadeType.ALL;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.utils.Copyable;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.envers.Audited;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.property.entity.MikEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;

/**
 * Entity implementation class for Entity: AmwFunction
 */
@Entity
@Audited
@Table(name = "TAMW_function")
public class AmwFunctionEntity implements Serializable, Copyable<AmwFunctionEntity> {

	private static final String COMMASEPARATOR = ",";

	@Getter
	@Setter
	@TableGenerator(name = "functionIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "functionId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "functionIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;

	@Getter
	@Column(nullable = false)
	private String name;

	@Getter
	@Setter
	@Lob
	private String implementation;

	@Getter
	@ManyToOne
	private ResourceEntity resource;

	@Getter
	@ManyToOne
	private ResourceTypeEntity resourceType;


    @Getter
    @ManyToOne
    private AmwFunctionEntity overwrittenParent;


    @OneToMany(mappedBy="overwrittenParent", cascade=ALL)
    private Set<AmwFunctionEntity> overwritingChildFunction;


    public Set<AmwFunctionEntity> getOverwritingChildFunction() {
        if (overwritingChildFunction == null){
            return new HashSet<>();
        }
        return Collections.unmodifiableSet(overwritingChildFunction);
    }

    void addOverwritingFunction(AmwFunctionEntity overwritingFunction){
       if( this.overwritingChildFunction == null){
           this.overwritingChildFunction = new HashSet<>();
       }
        this.overwritingChildFunction.add(overwritingFunction);
    }

    void removeOverwritingFunction(AmwFunctionEntity overwritingFunction){
        if (this.overwritingChildFunction != null){
            this.overwritingChildFunction.remove(overwritingFunction);
        }
    }


	@Setter
	@OneToMany(mappedBy = "amwFunction", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<MikEntity> miks;

	private static final long serialVersionUID = 1L;

    public void setName(String name) {
		if (name != null) {
			name = name.trim();
			if (!name.isEmpty()) {
				this.name = name;
			}
		}
	}


	@Override
	public String toString() {
		return "Function [id=" + id + ", name=" + name + "]";
	}

	public boolean isDefinedOnResource() {
		return resource != null;
	}

	public boolean isDefinedOnResourceType() {
		return resourceType != null;
	}


	private Set<MikEntity> getMiks() {
		if (miks == null) {
			return new HashSet<>();
		}
		return miks;
	}



    /**
     * Get all mik names
     */
    public Set<String> getMikNames() {
        Set<String> mikNames = new HashSet<>();
        for (MikEntity mik : getMiks()){
            mikNames.add(mik.getName());
        }
        return mikNames;
    }

	public String getCommaseparatedMikNames() {
		StringBuilder sb = new StringBuilder();
		for (MikEntity mik : getMiks()) {
			sb.append(" ").append(mik.getName()).append(COMMASEPARATOR);
		}
		String miks = sb.toString();
        if (!miks.isEmpty()) {
            return miks.substring(0, (miks.length() - COMMASEPARATOR.length()));
        }
        return miks;
	}

	/**
	 * Set resource entity on which the function is defined and set resourceType null because function can
	 * only be defined on either one
	 */
	public void setResource(ResourceEntity resource) {
		this.resource = resource;
		this.resourceType = null;
	}

	/**
	 * Set resourceType entity on which the function is defined and set resource null because function can
	 * only be defined on either one
	 */
	public void setResourceType(ResourceTypeEntity resourceType) {
		this.resourceType = resourceType;
		this.resource = null;
	}

    /**
     * Returns true if this function overwrites a function on a parent {@link ResourceTypeEntity}
     */
    public boolean isOverwritingResourceTypeFunction(){
        return overwrittenParent != null;
    }

    /**
     * Returns true if the function is is overwritten by a "sub" function defined within the subtree {@link ResourceTypeEntity#getChildrenResourceTypes()} or {@link ResourceTypeEntity#getResources()} or {@link ResourceEntity}
     * @return
     */
    public boolean isOverwrittenBySubTypeOrResourceFunction(){
        return !getOverwritingChildFunction().isEmpty();
    }

    /**
     * Returns the name of the {@link ResourceTypeEntity} where the overwritten functions belongs to
     */
    public String getOverwrittenFunctionResourceTypeName(){
        return isOverwritingResourceTypeFunction() ? overwrittenParent.getResourceType().getName() : "";
    }


    public void overwrite(AmwFunctionEntity functionToOverwrite){
        if (this.overwrittenParent != null){
            this.overwrittenParent.removeOverwritingFunction(this);
        }
        this.overwrittenParent = functionToOverwrite;

        if (this.overwrittenParent != null){
            this.overwrittenParent.addOverwritingFunction(this);
        }
    }

    /**
     * Reset function overriding (use when delete overrining function)
     */
    public void resetOverwriting(){
        if (this.overwrittenParent != null){
            this.overwrittenParent.removeOverwritingFunction(this);
            this.overwrittenParent = null;
        }
    }

    /**
     * @return the Implementation surrounded with the freemarker specific header and footer
     */
    public String getDecoratedImplementation() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFreemarkerHeader());
        if(implementation != null) {
            sb.append(implementation);
        }
        sb.append(getFreemarkerFooter());
        return sb.toString();
    }

    /**
     * Header in Freemarker Syntax for function
     */
    public final String getFreemarkerHeader() {
        return "<#function " + name + " >";
    }

    /**
     * Footer in Freemarker Syntax for function
     */
    public final String getFreemarkerFooter() {
        return "</#function>";
    }

    public void addMik(MikEntity mikEntity) {
        if(miks == null){
            miks = new HashSet<>();
        }
        miks.add(mikEntity);
    }

    @Override
    public AmwFunctionEntity getCopy(AmwFunctionEntity target, CopyUnit copyUnit) {
        if(target == null){
            target = new AmwFunctionEntity();
        }
        target.setName(this.getName());
        target.setImplementation(this.getImplementation());
        target.setResource(copyUnit.getTargetResource());
        target.overwrite(this.getOverwrittenParent());
        for (String originMik : this.getMikNames()) {
            if (!target.getMikNames().contains(originMik)) {
                target.addMik(new MikEntity(originMik, target));
            }
        }
        return target;
    }
}
