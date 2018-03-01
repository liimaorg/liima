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

package ch.puzzle.itc.mobiliar.business.database.entity;

import ch.puzzle.itc.mobiliar.business.auditview.control.AuditService;
import ch.puzzle.itc.mobiliar.business.auditview.control.ThreadLocalUtil;
import org.hibernate.envers.RevisionListener;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class MyRevisionEntityListenerTest {

    RevisionListener liimaRevisionListener = new MyRevisionEntityListener();

    AuditService auditService = new AuditService();

    @Before
    public void init() {
        ThreadLocalUtil.destroy();
    }

    @Test
    public void shouldDestroyThreadLocalVariables() {
        // given
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        auditService.setResourceTypeIdInThreadLocal(100);
        auditService.setResourceIdInThreadLocal(50);

        // when
        liimaRevisionListener.newRevision(revisionEntity);

        // then
        assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_TYPE_ID), is(nullValue()));
        assertThat(ThreadLocalUtil.getThreadVariable(ThreadLocalUtil.KEY_RESOURCE_ID), is(nullValue()));
    }

    @Test
    public void shouldSetResourceIdOnRevisionEntity() {
        // given
        int resourceId = 44;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        auditService.setResourceIdInThreadLocal(resourceId);

        // when
        liimaRevisionListener.newRevision(revisionEntity);

        // then
        assertThat(revisionEntity.getResourceId(), is(resourceId));
        assertThat(revisionEntity.getResourceTypeId(), is(nullValue()));
    }

    @Test
    public void shouldSetResourceTypeIdOnRevisionEntity() {
        // given
        int resourceTypeId = 2;
        MyRevisionEntity revisionEntity = new MyRevisionEntity();
        auditService.setResourceTypeIdInThreadLocal(resourceTypeId);

        // when
        liimaRevisionListener.newRevision(revisionEntity);

        // then
        assertThat(revisionEntity.getResourceId(), is(nullValue()));
        assertThat(revisionEntity.getResourceTypeId(), is(resourceTypeId));
    }
}
