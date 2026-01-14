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

package ch.puzzle.itc.mobiliar.presentation.resourcesedit;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataProviderHelperTest {

    private DataProviderHelper helper = new DataProviderHelper();

    @Test
    public void testNextFreeIdentifier2() {
        // without postfix
        assertEquals("db2", helper.nextFreeIdentifier(Collections.<String>emptyList(), "db2", null));

        // first with postfix
        List<String> list = new ArrayList<String>();
        list.add("db2");
        assertEquals("db2_1", helper.nextFreeIdentifier(list, "db2", null));

        // second with postfix
        list = new ArrayList<String>();
        list.add("db2");
        list.add("db2_4");
        assertEquals("db2_2", helper.nextFreeIdentifier(list, "db2", null));

        // null prefix
        assertEquals("_2", helper.nextFreeIdentifier(list, null, null));

        // to lower case
        assertEquals("NODE_2", helper.nextFreeIdentifier(list, "NODE", null));
    }

    @Test
    public void shouldReturnNullIfResourceEditRelationListIsEmpty() {
        // given
        List<ResourceEditRelation> relations = Collections.<ResourceEditRelation>emptyList();

        // when
        String actual = helper.nextFreeIdentifierForResourceEditRelations(relations, null, StringUtils.EMPTY);

        // then
        assertEquals(StringUtils.EMPTY, actual);
    }

    @Test
    public void shouldReturnNameUnderscoreOneIfSlaveGroupContainsOneElement() {
        // given
        List<ResourceEditRelation> relations = new ArrayList<>();
        ResourceEditRelation rel0 = new ResourceEditRelation(null, null, null, "slaveName",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        relations.add(rel0);

        // when // then
        assertEquals("slaveName_1",
                helper.nextFreeIdentifierForResourceEditRelations(relations, 21, StringUtils.EMPTY));
    }

    @Test
    public void shouldReturnNameUnderscoreTwoIfSlaveGroupContainsTwoElements() {
        // given
        List<ResourceEditRelation> relations = new ArrayList<>();
        ResourceEditRelation rel0 = new ResourceEditRelation(null, null, null, "slaveName",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation rel1 = new ResourceEditRelation(null, null, null, "slaveName_1",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        relations.add(rel0);
        relations.add(rel1);

        // when // then
        assertEquals("slaveName_2",
                helper.nextFreeIdentifierForResourceEditRelations(relations, 21, StringUtils.EMPTY));
    }

    @Test
    public void shouldReturnNameUnderscoreThreeIfSlaveGroupContainsThreeElements() {
        // given
        List<ResourceEditRelation> relations = new ArrayList<>();
        ResourceEditRelation rel0 = new ResourceEditRelation(null, null, null, "slaveName",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation rel1 = new ResourceEditRelation(null, null, null, "slaveName_1",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation rel2 = new ResourceEditRelation(null, null, null, "custom",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        relations.add(rel0);
        relations.add(rel1);
        relations.add(rel2);

        // when // then
        assertEquals("slaveName_3",
                helper.nextFreeIdentifierForResourceEditRelations(relations, 21, StringUtils.EMPTY));
    }

    @Test
    public void shouldIgnoreElementsWithOtherSlaveGroupId() {
        // given
        List<ResourceEditRelation> relations = new ArrayList<>();
        ResourceEditRelation rel0 = new ResourceEditRelation(null, null, null, "slaveName",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation rel1 = new ResourceEditRelation(null, null, null, "slaveName_1",
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation rel2 = new ResourceEditRelation(null, null, null, "slaveName",
                        23, null, null, null, null, null, null, null, "CONSUMED", null);
        relations.add(rel0);
        relations.add(rel1);
        relations.add(rel2);

        // when // then
        assertEquals("slaveName_2",
                helper.nextFreeIdentifierForResourceEditRelations(relations, 21, StringUtils.EMPTY));
    }

    @Test
    public void shouldUseGivenPrefixWhenNoRelations() {
        // given
        List<ResourceEditRelation> relations = new ArrayList<>();
        String prefix = "activemq";

        // when
        String s = helper.nextFreeIdentifierForResourceEditRelations(relations, 21, prefix);

        // then
        assertThat(s, is(prefix));
    }

    @Test
    public void shouldUseGivenPrefixWhenNoRelationsInSameSlaveGroup() {
        // given
        List<ResourceEditRelation> relations = new ArrayList<>();
        ResourceEditRelation rel0 = new ResourceEditRelation(null, null, null, "slaveName",
                        1, null, null, null, null, null, null, null,"CONSUMED", null);
        relations.add(rel0);
        String prefix = "activemq";

        // when
        String s = helper.nextFreeIdentifierForResourceEditRelations(relations, 21, prefix);

        // then
        assertThat(s, is(prefix));
    }

    @Test
    public void shouldUseNextIdentifierForRelationInSameSlaveGroup() {
        // given
        String slaveName = "activeMq";
        String prefix = slaveName;
        List<ResourceEditRelation> relations = new ArrayList<>();
        ResourceEditRelation rel0 = new ResourceEditRelation(null, null, null, slaveName,
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        ResourceEditRelation rel1 = new ResourceEditRelation(null, null, null, slaveName,
                        21, null, null, null, null, null, null, null, "CONSUMED", null);
        relations.add(rel0);
        relations.add(rel1);

        // when
        String s = helper.nextFreeIdentifierForResourceEditRelations(relations, 21, prefix);

        // then
        assertThat(s, is(String.format("%s_2", prefix)));
    }

}
