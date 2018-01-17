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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Entity
@Audited()
@Table(name = "TAMW_consumedResRel")
public class ConsumedResourceRelationEntity extends AbstractResourceRelationEntity implements Auditable {

    // IMPORTANT! Whenever a new field (not relation to other entity) is added then this field must be added to foreignableFieldEquals method!!!


    @OneToMany(mappedBy = "consumedResourceRelation", cascade = ALL)
    private Set<ResourceRelationContextEntity> contexts;


    /**
     * Creates new entity object with default system owner
     */

    public ConsumedResourceRelationEntity() {
        super(ForeignableOwner.getSystemOwner());
    }

    public ConsumedResourceRelationEntity(ForeignableOwner owner) {
        super(Objects.requireNonNull(owner, "Owner must not be null"));
    }

    @Override
    public Set<ResourceRelationContextEntity> getContexts() {
        return contexts;
    }

    @Override
    public void setContexts(Set<ResourceRelationContextEntity> contexts) {
        this.contexts = contexts;
    }

    @Override
    public String toString() {
        return "ConsumedResourceRelationEntity [getId()=" + getId() + ", getMasterResource()=" + getMasterResource() + ", getSlaveResource()="
                + getSlaveResource() + "]";
    }

    public String getMasterResourceName() {
        return getMasterResource().getName();
    }

    public Integer getMasterResourceId() {
        return getMasterResource().getId();
    }

    public Integer getSlaveResourceTypeId() {
        return this.getSlaveResource().getResourceType().getId();
    }

    public String getRelationIdentifier() {
        return getIdentifier() != null ? getIdentifier() : getResourceRelationType().getIdentifierOrTypeBName();
    }

    public String getMasterRelease() {
        return getMasterResource().getRelease().getName();
    }

    @Override
    protected int foreignableRelationFieldHashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.toHashCode();
    }

    @Override
    public ConsumedResourceRelationEntity getCopy(AbstractResourceRelationEntity target, CopyUnit copyUnit) {
        boolean isMasterRelation = isMasterResource(copyUnit.getOriginResource());
        // only Copy AMW owned Relations and the target is null, if target is set we need to proceed to also add values
        if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR
                && !ForeignableOwner.getSystemOwner().isSameOwner(this.getOwner()) && target == null) {
            return null;
        }

        // slave relations will be only copied in RELEASE mode
        if (isMasterRelation || copyUnit.getMode() == CopyResourceDomainService.CopyMode.RELEASE) {
            ConsumedResourceRelationEntity consumedTarget;
            if (copyUnit.getMode() == CopyResourceDomainService.CopyMode.COPY && copyUnit.getOriginResource().getResourceType().isApplicationServerResourceType()
                    && getSlaveResource().getResourceType().isApplicationResourceType()) {
                // an application can only belong to one as - do not copy the as->app relation
                copyUnit.getResult().addSkippedConsumedRelation(getId(),
                        getMasterResource().getName(), getSlaveResource().getName(),
                        getIdentifier(), getMasterResource().getResourceType().getName(),
                        getSlaveResource().getResourceType().getName());
                return null;
            }

            if (target == null) {
                consumedTarget = new ConsumedResourceRelationEntity();
            } else {
                consumedTarget = (ConsumedResourceRelationEntity) target;
            }

            consumedTarget.setIdentifier(getIdentifier());

            if (isMasterRelation) {
                // master relation
                consumedTarget.setMasterResource(copyUnit.getTargetResource());
                if (CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR != copyUnit.getMode()
                        || consumedTarget.getSlaveResource() == null) {
                    consumedTarget.setSlaveResource(getSlaveResource());
                }
            } else {
                // slave relation
                consumedTarget.setMasterResource(getMasterResource());
                consumedTarget.setSlaveResource(copyUnit.getTargetResource());
            }
            consumedTarget.setResourceRelationType(getResourceRelationType());
            CopyHelper.copyForeignable(consumedTarget, this, copyUnit);

            return consumedTarget;
        }
        return null;
    }

    @Override
    public String getNewValueForAuditLog() {
        return getIdentifier() == null ? StringUtils.EMPTY : String.format("RelationName: \"%s\"", getIdentifier());
    }

    @Override
    public String getType() {
        return Auditable.TYPE_CONSUMED_RESOURCE_RELATION;
    }

    @Override
    public String getNameForAuditLog() {
        return String.format("Consumed Resource: '%s'", this.getSlaveResource().getName());
    }
}
