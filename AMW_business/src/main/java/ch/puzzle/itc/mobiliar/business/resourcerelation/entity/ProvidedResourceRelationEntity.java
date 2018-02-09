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

package ch.puzzle.itc.mobiliar.business.resourcerelation.entity;

import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.utils.CopyHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Entity
@Audited
@Table(name = "TAMW_providedResRel")
public class ProvidedResourceRelationEntity extends AbstractResourceRelationEntity implements Auditable {

    // IMPORTANT! Whenever a new field (not relation to other entity) is added then this field must be added to foreignableFieldEquals method!!!
	
	@Getter
	@Setter
	@OneToMany(mappedBy = "providedResourceRelation", cascade = ALL)
	private Set<ResourceRelationContextEntity> contexts;

    /**
     * Creates new entity object with default system owner
     */
    public ProvidedResourceRelationEntity() {
        super(ForeignableOwner.getSystemOwner());
    }

    public ProvidedResourceRelationEntity(ForeignableOwner owner) {
        super(Objects.requireNonNull(owner, "Owner must not be null"));
    }

    @Override
    protected int foreignableRelationFieldHashCode() {
        return 0;
    }

    @Override
    public ProvidedResourceRelationEntity getCopy(AbstractResourceRelationEntity target, CopyUnit copyUnit) {
        boolean isMasterRelation = isMasterResource(copyUnit.getOriginResource());

        // only Copy AMW owned Relations and the target is null, if target is set we need to proceed to also add values
        if(copyUnit.getMode() == CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR
                && !ForeignableOwner.getSystemOwner().isSameOwner(this.getOwner()) && target == null){
            return null;
        }

		// slave relations will be only copied in RELEASE mode
        if(isMasterRelation || copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
            ProvidedResourceRelationEntity targetCopy = null;
            if (target == null) {
                targetCopy = new ProvidedResourceRelationEntity();
            } else {
                targetCopy = (ProvidedResourceRelationEntity) target;
            }

            targetCopy.setIdentifier(getIdentifier());
            if (isMasterRelation) {
                // master relation
                targetCopy.setMasterResource(copyUnit.getTargetResource());
                if(CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR != copyUnit.getMode()
                        || targetCopy.getSlaveResource() == null){
                    // Maia Predecessor Mode, the target Slave Resource must remain
                    targetCopy.setSlaveResource(getSlaveResource());
                }

            } else {
                // slave relation
                targetCopy.setMasterResource(getMasterResource());
                targetCopy.setSlaveResource(copyUnit.getTargetResource());
            }
            targetCopy.getSlaveResource().addProvidedSlaveRelation(targetCopy);
            targetCopy.setResourceRelationType(getResourceRelationType());
            CopyHelper.copyForeignable(targetCopy, this, copyUnit);

            return targetCopy;
        }
        return null;
    }


    @Override
    public String getNewValueForAuditLog() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getType() {
        return Auditable.TYPE_PROVIDED_RESOURCE_RELATION;
    }

    @Override
    public String getNameForAuditLog() {
        return String.format("Provided Resource: '%s'", this.getSlaveResource().getName());
    }

    @Override
    public boolean isObfuscatedValue() {
        return false;
    }
}
