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

package ch.puzzle.itc.mobiliar.presentation.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.puzzle.itc.mobiliar.business.generator.control.GeneratedTemplate;

@ExtendWith(MockitoExtension.class)
public class ComparedGeneratedTemplatesTest {

    @Mock
    GeneratedTemplate originalTemplate;

    @Mock
    GeneratedTemplate comparedTemplate;

    @Test
    public void testHasOriginalTemplateTrue() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setOriginalTemplate(originalTemplate);
        assertTrue(t.hasOriginalTemplate());
    }

    @Test
    public void testHasOriginalTemplateFalse() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        assertFalse(t.hasOriginalTemplate());
    }

    @Test
    public void testHasComparedTemplateTrue() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setComparedTemplate(comparedTemplate);
        assertTrue(t.hasComparedTemplate());
    }

    @Test
    public void testHasComparedTemplateFalse() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        assertFalse(t.hasComparedTemplate());
    }

    @Test
    public void testSameContent() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setOriginalTemplate(originalTemplate);
        t.setComparedTemplate(comparedTemplate);
        Mockito.when(originalTemplate.getContent()).thenReturn("sameContent");
        Mockito.when(originalTemplate.isSameContent(Mockito.any(GeneratedTemplate.class))).thenReturn(true);
        assertTrue(t.sameContent());
    }

    @Test
    public void testSameContentNull() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setOriginalTemplate(originalTemplate);
        t.setComparedTemplate(comparedTemplate);
        Mockito.when(originalTemplate.getContent()).thenReturn(null);
        Mockito.when(comparedTemplate.getContent()).thenReturn(null);
        assertTrue(t.sameContent());
    }

    @Test
    public void testSameContentNOK() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setOriginalTemplate(originalTemplate);
        t.setComparedTemplate(comparedTemplate);
        Mockito.when(originalTemplate.getContent()).thenReturn("someContent");
        assertFalse(t.sameContent());
    }

    @Test
    public void testSameContentNOKNull1() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setOriginalTemplate(originalTemplate);
        t.setComparedTemplate(comparedTemplate);
        Mockito.when(originalTemplate.getContent()).thenReturn(null);
        Mockito.when(comparedTemplate.getContent()).thenReturn("anotherContent");
        assertFalse(t.sameContent());
    }

    @Test
    public void testSameContentNOKNull2() {
        ComparedGeneratedTemplates t = new ComparedGeneratedTemplates(null);
        t.setOriginalTemplate(originalTemplate);
        t.setComparedTemplate(comparedTemplate);
        Mockito.when(originalTemplate.getContent()).thenReturn("someContent");
        assertFalse(t.sameContent());
    }

    @Test
    public void testCompareTo() {
        ComparedGeneratedTemplates t1 = new ComparedGeneratedTemplates("aaa");
        ComparedGeneratedTemplates t2 = new ComparedGeneratedTemplates("bbb");
        assertEquals(-1, t1.compareTo(t2));
        assertEquals(1, t2.compareTo(t1));
    }

    @Test
    public void testCompareToWithNullPath() {
        ComparedGeneratedTemplates t1 = new ComparedGeneratedTemplates("aaa");
        ComparedGeneratedTemplates t2 = new ComparedGeneratedTemplates(null);
        assertEquals(1, t1.compareTo(t2));
        assertEquals(-1, t2.compareTo(t1));
    }

    @Test
    public void testCompareToWithBothNullPath() {
        ComparedGeneratedTemplates t1 = new ComparedGeneratedTemplates(null);
        ComparedGeneratedTemplates t2 = new ComparedGeneratedTemplates(null);
        assertEquals(0, t1.compareTo(t2));
    }

    @Test
    public void testCompareToWithNull() {
        ComparedGeneratedTemplates t1 = new ComparedGeneratedTemplates(null);
        assertEquals(1, t1.compareTo(null));
    }

}
