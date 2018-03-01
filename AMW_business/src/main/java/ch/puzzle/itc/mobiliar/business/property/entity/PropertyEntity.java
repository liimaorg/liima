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
import ch.puzzle.itc.mobiliar.business.environment.entity.AbstractContext;
import ch.puzzle.itc.mobiliar.business.generator.control.TemplateUtils;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.utils.Copyable;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Entity implementation class for Entity: Property
 *
 */
@Entity
@Audited
@Table(name="TAMW_property")
public class PropertyEntity implements Identifiable, Serializable, Copyable<PropertyEntity>, Auditable {
	   
	@Getter
	@Setter
	@TableGenerator(name = "propertyIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants.GENERATORVALUECOLUMNNAME, pkColumnValue = "propertyId")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "propertyIdGen")
	@Id
	@Column(unique = true, nullable = false)
	private Integer id;
	
	@Getter
	@Setter
	@ManyToOne(optional = false, fetch=FetchType.LAZY)
	private PropertyDescriptorEntity descriptor;
	
	
	@Getter
	@Lob
	private String value;

	private static final long serialVersionUID = 1L;	
	
	@Getter
	@Setter
	@Transient
	private AbstractContext owningResource;

	@Transient
	private String decryptedValue;
	

	@Getter
	@Version
	private long v;
	
	public PropertyEntity() {
		super();
	}   
	
	public String getDecryptedValue(){
		if(getDescriptor()!=null && getDescriptor().isEncrypt()){
			if(decryptedValue==null && value!=null) {
				decryptedValue = TemplateUtils.decrypt(value);
			}
			return decryptedValue;
		} else {
			return value;
		}		
	}

    /**
	* Encrypts and replaces the current value of the property (regardless if it has been encrypted before)
	*/
    public void encrypt(){
	  value = TemplateUtils.encrypt(value);
	}

    /**
	* Decrypts and replaces the current value of the property (regardless if it has been encrypted before)
	*/
    public void decrypt(){
	   value = TemplateUtils.decrypt(value);
    }
	
	public void setValueAndEncrypt(String value){
		if(getDescriptor()!=null && getDescriptor().isEncrypt()){
			value = TemplateUtils.encrypt(value);			
		}		
		setValue(value);
	}

	public void setValue(String value) {
		if (value != null) {
			this.value = removeLineBreaks(value);
		}
	}

	public static void createProperty(String value, PropertyDescriptorEntity propertyDescriptor, AbstractContext owningResource){
		PropertyEntity prop = new PropertyEntity();
		prop.descriptor = propertyDescriptor;
		prop.owningResource = owningResource;
		prop.setValueAndEncrypt(value);
		owningResource.addProperty(prop);
	}	
	
	public PropertyEntity copy(){
		PropertyEntity prop = new PropertyEntity();
		prop.setDescriptor(getDescriptor());
		prop.setValue(getValue());
		return prop;
	}

	@Override
	public String toString() {
		return "PropertyEntity [value=" + value + "]";
	}

    public static final Comparator<PropertyEntity> DESCRIPTOR_COMPARATOR = new Comparator<PropertyEntity>() {

	   @Override
	   public int compare(PropertyEntity o1, PropertyEntity o2) {
		  if(o1==null){
			 return -1;
		  }
		  else if(o2==null){
			 return 1;
		  }
		  else{
			 return PropertyDescriptorEntity.CARDINALITY_AND_NAME_COMPARATOR.compare(o1.getDescriptor(), o2.getDescriptor());
		  }
	   }
    };

    /**
     * @return the converted Freemarker Property
     */
    public FreeMarkerProperty toFreemarkerProperty(){
    	String propertyValue = "";
    	
    	if(hasValue()){
    		propertyValue = getDecryptedValue();
    	}
    	if(!descriptor.isOptional() && !hasValue()){
    		// take defaultValue as Value
    		propertyValue = descriptor.getDefaultValue();
    	}
    	
    	return new FreeMarkerProperty(propertyValue, descriptor);
    }

	/**
	 * @return true if a value is defined (not null and not Empty String) 
	 */
	public boolean hasValue() {
		String value = getDecryptedValue();
		return value != null && !value.isEmpty();
	}

	private String removeLineBreaks(String input) {
		return input.replaceAll("\\r\\n|\\r|\\n", " ");
	}

	@Override
	public PropertyEntity getCopy(PropertyEntity target, CopyUnit copyUnit) {
		if(target == null) {
			target = new PropertyEntity();
		}

		target.setValue(getValue());

		if (target.getDescriptor() == null) {
			target.setDescriptor(getDescriptor());
		}
		target.getDescriptor().addProperty(target);
		return target;
	}

	@Override
	public String getNewValueForAuditLog() {
		return this.getValue();
	}

	@Override
	public String getType() {
		return Auditable.TYPE_PROPERTY;
	}

	@Override
	public String getNameForAuditLog() {
		return this.getDescriptor().getPropertyName();
	}

	@Override
	public boolean isObfuscatedValue() {
		return this.descriptor.isEncrypt();
	}
}
