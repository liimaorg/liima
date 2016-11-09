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

import org.junit.Test;

import static org.junit.Assert.*;

public class FreeMarkerPropertyTest {

    @Test
    public void testCompareTo() throws Exception {
        // given
        FreeMarkerProperty p1 = new FreeMarkerProperty("b", "key");
        FreeMarkerProperty p2 = new FreeMarkerProperty("a", "key");
        // when
        int i = p1.compareTo(p2);

        // then
        assertEquals(1, i);
    }

    @Test
    public void testGetAmwProperty() throws Exception {
        // given
        FreeMarkerProperty p = new FreeMarkerProperty("", "key");

        // then
        assertTrue(p.getAmwProperty());
    }

    @Test
    public void testGetCurrentValue() throws Exception {
        // given
        FreeMarkerProperty p = new FreeMarkerProperty("$${test}", "key");

        // then
        assertEquals("$${test}", p.getCurrentValue());
    }
    @Test
    public void testGetCurrentValue_with_Descriptor() throws Exception {
        // given
        FreeMarkerProperty p = new FreeMarkerProperty("$${test}", new PropertyDescriptorEntity());

        // then
        assertEquals("$${test}", p.getCurrentValue());
    }

    @Test
    public void testGet_descriptor() throws Exception {
        // given
        PropertyDescriptorEntity des = new PropertyDescriptorEntity();
        des.setPropertyName("techKey");
        FreeMarkerProperty p = new FreeMarkerProperty("$${test}", des);

        // then
        assertEquals("$${test}", p.getCurrentValue());
        assertEquals("techKey", p.get_descriptor().getTechnicalKey());
    }
    @Test
    public void testGet_descriptor_null() throws Exception {
        // given
        PropertyDescriptorEntity des = null;
        FreeMarkerProperty p = new FreeMarkerProperty("$${test}", des);

        // then
        assertEquals("$${test}", p.getCurrentValue());
        assertNull(p.get_descriptor());
    }

    @Test
    public void testToString() throws Exception {
        // given
        FreeMarkerProperty p = new FreeMarkerProperty("$${test}", "key");

        // then
        assertEquals("$${test}", p.toString());
    }
    @Test
    public void testToString_null() throws Exception {
        // given
        FreeMarkerProperty p = new FreeMarkerProperty(null, "null");

        // then
        assertEquals("", p.toString());
    }

    @Test
    public void testDefaultPropertyDescriptor() throws Exception {
        // given
        FreeMarkerProperty p = new FreeMarkerProperty("value", "key");

        // then
        assertEquals("value", p.toString());
        assertEquals("key", p.get_descriptor().getDisplayName());
        assertEquals("key", p.get_descriptor().getTechnicalKey());
    }
}