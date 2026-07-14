/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2026 by Puzzle ITC
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

package ch.puzzle.itc.mobiliar.business.property.boundary;

import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditRelation;
import ch.puzzle.itc.mobiliar.business.resourcerelation.boundary.ResourceRelationMapper;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetRelationPropertiesServiceTest {

    @InjectMocks
    GetRelationPropertiesService service;

    @Mock
    ResourceRelationMapper resourceRelationMapper;

    @Mock
    PropertyEditor propertyEditor;


    @Test
    public void shouldThrowWhenRelationNotFound() throws ResourceNotFoundException {
        // given
        when(resourceRelationMapper.toResourceEditRelation(99)).thenThrow(new ResourceNotFoundException("Relation with id 99 not found"));

        // when / then
        assertThrows(ResourceNotFoundException.class,
                () -> service.getPropertiesForRelation(1, 99, 1));
    }

    @Test
    public void shouldDelegateToPropertyEditorForConsumedRelation() throws ResourceNotFoundException {
        // given
        ResourceEditRelation resourceEditRelation = mock(ResourceEditRelation.class);
        when(resourceEditRelation.getMode()).thenReturn(ResourceEditRelation.Mode.CONSUMED);
        when(resourceRelationMapper.toResourceEditRelation(20)).thenReturn(resourceEditRelation);

        List<ResourceEditProperty> expected = Collections.emptyList();
        when(propertyEditor.getPropertiesForRelatedResource(eq(10), any(ResourceEditRelation.class), eq(1)))
                .thenReturn(expected);

        // when
        List<ResourceEditProperty> result = service.getPropertiesForRelation(10, 20, 1);

        // then
        assertSame(expected, result);
        ArgumentCaptor<ResourceEditRelation> captor = ArgumentCaptor.forClass(ResourceEditRelation.class);
        verify(propertyEditor).getPropertiesForRelatedResource(eq(10), captor.capture(), eq(1));
        assertEquals(ResourceEditRelation.Mode.CONSUMED, captor.getValue().getMode());

    }

    @Test
    public void shouldDelegateToPropertyEditorForProvidedRelation() throws ResourceNotFoundException {
        // given
        ResourceEditRelation resourceEditRelation = mock(ResourceEditRelation.class);
        when(resourceEditRelation.getMode()).thenReturn(ResourceEditRelation.Mode.PROVIDED);
        when(resourceRelationMapper.toResourceEditRelation(30)).thenReturn(resourceEditRelation);

        List<ResourceEditProperty> expected = Collections.singletonList(mock(ResourceEditProperty.class));
        when(propertyEditor.getPropertiesForRelatedResource(eq(5), any(ResourceEditRelation.class), eq(2)))
                .thenReturn(expected);

        // when
        List<ResourceEditProperty> result = service.getPropertiesForRelation(5, 30, 2);

        // then
        assertSame(expected, result);
        ArgumentCaptor<ResourceEditRelation> captor = ArgumentCaptor.forClass(ResourceEditRelation.class);
        verify(propertyEditor).getPropertiesForRelatedResource(eq(5), captor.capture(), eq(2));
        assertEquals(ResourceEditRelation.Mode.PROVIDED, captor.getValue().getMode());
    }
}
