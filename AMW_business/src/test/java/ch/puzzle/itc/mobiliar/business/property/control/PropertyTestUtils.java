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

package ch.puzzle.itc.mobiliar.business.property.control;

import javax.persistence.EntityManager;

import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceContextEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;

public class PropertyTestUtils {


    public static void persistExamplePropertyDescriptorList(EntityManager entityManager, ResourceEntity res, ContextEntity globalContext) {
        PropertyDescriptorEntity prop1 = new PropertyDescriptorEntity();
        prop1.setPropertyName("zzzz");
        PropertyDescriptorEntity prop2 = new PropertyDescriptorEntity();
        // Cardinality zero is regarded to be null
        prop2.setCardinalityProperty(0);
        prop2.setPropertyName("aaa");
        PropertyDescriptorEntity propCar = new PropertyDescriptorEntity();
        propCar.setCardinalityProperty(-1);
        ResourceContextEntity ctx = res.getOrCreateContext(globalContext);
        ctx.addPropertyDescriptor(prop1);
        ctx.addPropertyDescriptor(prop2);
        ctx.addPropertyDescriptor(propCar);
        entityManager.persist(prop1);
        entityManager.persist(prop2);
        entityManager.persist(propCar);
    }
}
