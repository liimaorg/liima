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

package ch.puzzle.itc.mobiliar.business.softlinkRelation.entity;

import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.PERSIST;

import javax.persistence.*;

import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.envers.Audited;

import ch.puzzle.itc.mobiliar.business.database.control.Constants;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.Foreignable;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyUnit;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.utils.CopyHelper;
import ch.puzzle.itc.mobiliar.business.utils.Identifiable;

/**
 * Entity that describes the relation between a CPI resource and a PPI resource.
 */
@Entity
@Audited()
@Table(name = "TAMW_SOFTLINKRELATION")
public class SoftlinkRelationEntity implements Identifiable, Foreignable<SoftlinkRelationEntity> {

    @Getter
    @Setter
    @TableGenerator(name = "softlinkRelationIdGen", table = Constants.GENERATORTABLE, pkColumnName = Constants.GENERATORPKCOLUMNNAME, valueColumnName = Constants
            .GENERATORVALUECOLUMNNAME, pkColumnValue = "softlinkRelationId")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "softlinkRelationIdGen")
    @Id
    @Column(unique = true, nullable = false)
    private Integer id;

    @Getter
    @Setter
    private String softlinkRef;

    @Getter
    @Setter
    @ManyToOne(cascade = { PERSIST, DETACH })
    private ResourceEntity cpiResource;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "FCOWNER")
    private ForeignableOwner owner;

    @Getter
    @Setter
    @Column(name = "FCEXTERNALKEY")
    private String externalKey;

    @Getter
    @Setter
    @Column(name = "FCEXTERNALLINK")
    private String externalLink;

    @Version
    @Getter
    private long v;

    @Override
	public SoftlinkRelationEntity getCopy(SoftlinkRelationEntity target, CopyUnit copyUnit) {
		if (target == null) {
			target = new SoftlinkRelationEntity();
		}
        // only set softlink rel on target if not in Predecessor mode
        if(!CopyResourceDomainService.CopyMode.MAIA_PREDECESSOR.equals(copyUnit.getMode())){
            target.setSoftlinkRef(this.softlinkRef);
        }
		target.setCpiResource(copyUnit.getTargetResource());
		CopyHelper.copyForeignable(target, this, copyUnit);

		return target;
	}

    @Override
    public String getForeignableObjectName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int foreignableFieldHashCode() {
        HashCodeBuilder eb = new HashCodeBuilder();

        eb.append(this.id);
        eb.append(this.owner);
        eb.append(this.externalKey);
        eb.append(this.externalLink);
        eb.append(this.softlinkRef);
        eb.append(this.cpiResource != null ? cpiResource.getId() : null);

        return eb.toHashCode();
    }

}
