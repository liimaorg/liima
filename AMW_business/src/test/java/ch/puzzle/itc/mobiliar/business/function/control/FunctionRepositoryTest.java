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

package ch.puzzle.itc.mobiliar.business.function.control;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.MockitoJUnitRunner;

import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntity;
import ch.puzzle.itc.mobiliar.business.function.entity.AmwFunctionEntityBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FunctionRepositoryTest {

	@Mock
	private EntityManager entityManagerMock;

	@InjectMocks
	private FunctionRepository functionRepository;

	@Before
	public void setup() {
		functionRepository.entityManager = entityManagerMock;
	}

	@Test
	public void persistOrMergeFunctionWhenFunctionHasIdShouldDelegateMerge() {
		// given
		Integer id = 1;
		AmwFunctionEntity function = new AmwFunctionEntityBuilder("name", id).build();

		// when
		functionRepository.persistOrMergeFunction(function);

		// then
        Mockito.verify(functionRepository.entityManager).merge(function);
	}

    @Test
    public void persistOrMergeFunctionWhenFunctionHasNoIdShouldDelegatePersist() {
        // given
        Integer id = null;
        AmwFunctionEntity function = new AmwFunctionEntityBuilder("name", id).build();

        // when
        functionRepository.persistOrMergeFunction(function);

        // then
        Mockito.verify(functionRepository.entityManager).persist(function);
    }

}