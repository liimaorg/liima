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

package ch.mobi.itc.mobiliar.rest.resources;

import ch.mobi.itc.mobiliar.rest.dtos.PropertyExtendedDTO;
import ch.puzzle.itc.mobiliar.business.environment.boundary.ContextLocator;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.property.boundary.GetRelationPropertiesUseCase;
import ch.puzzle.itc.mobiliar.business.property.entity.ResourceEditProperty;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceRelationsByIdRestTest {

    @InjectMocks
    ResourceRelationsByIdRest rest;

    @Mock
    GetRelationPropertiesUseCase getRelationPropertiesUseCase;

    @Mock
    ContextLocator contextLocator;

    @Test
    public void getRelationProperties_shouldPropagateNotFoundExceptionIfContextNotFound() throws ResourceNotFoundException, NotFoundException {
        // given
        when(contextLocator.getById(1)).thenThrow(new NotFoundException("Context not found"));

        // when / then
        org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class,
                () -> rest.getRelationProperties(10, 20, 1));
    }

    @Test
    public void getRelationProperties_shouldReturnOkWithEmptyListIfNoProperties() throws ResourceNotFoundException, NotFoundException {
        // given
        ContextEntity context = new ContextEntity();
        context.setName("Global");
        when(contextLocator.getById(1)).thenReturn(context);
        when(getRelationPropertiesUseCase.getPropertiesForRelation(10, 20, 1)).thenReturn(Collections.emptyList());

        // when
        Response response = rest.getRelationProperties(10, 20, 1);

        // then
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        List<?> body = (List<?>) response.getEntity();
        assertThat(body.isEmpty(), is(true));
    }

    @Test
    public void getRelationProperties_shouldReturnOkWithPropertyDtos() throws ResourceNotFoundException, NotFoundException {
        // given
        ContextEntity context = new ContextEntity();
        context.setName("Global");
        when(contextLocator.getById(1)).thenReturn(context);

        ResourceEditProperty prop = mock(ResourceEditProperty.class);
        when(prop.getTechnicalKey()).thenReturn("myKey");
        when(prop.getOriginOfValue(eq(1), any())).thenReturn("");
        when(getRelationPropertiesUseCase.getPropertiesForRelation(10, 20, 1))
                .thenReturn(Collections.singletonList(prop));

        // when
        Response response = rest.getRelationProperties(10, 20, 1);

        // then
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        @SuppressWarnings("unchecked")
        List<PropertyExtendedDTO> dtos = (List<PropertyExtendedDTO>) response.getEntity();
        assertEquals(1, dtos.size());
        assertEquals("myKey", dtos.get(0).getName());
    }

    @Test
    public void getRelationProperties_shouldPropagateResourceNotFoundException() throws ResourceNotFoundException, NotFoundException {
        // given
        ContextEntity context = new ContextEntity();
        context.setName("Global");
        when(contextLocator.getById(1)).thenReturn(context);
        when(getRelationPropertiesUseCase.getPropertiesForRelation(10, 99, 1))
                .thenThrow(new ResourceNotFoundException("Relation not found"));

        // when / then
        org.junit.jupiter.api.Assertions.assertThrows(ResourceNotFoundException.class,
                () -> rest.getRelationProperties(10, 99, 1));
    }
}
