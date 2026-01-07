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

package ch.puzzle.itc.mobiliar.business.domain.commons;

import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentOrder;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.domain.commons.Sort.SortingDirectionType;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;

import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith({MockitoExtension.class, PersistenceTestExtension.class})
public class CommonFilterServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	CommonFilterService service;

    @Test
    public void test_addFilterAndCreateQuery(){
        // given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ") ;
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = CustomFilter.builder(DEPLOYMENT_STATE)
                .enumType(DeploymentState.class)
                .isSelected(true)
                .build();
        filter.setValue("success");
        filters.add(filter);
        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, false);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);

        // when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, Sort.builder().order(colToSort).order(uniqueCol).build(), false, false);

        // then
        assertThat(query.getParameters().size(), is(1));
        assertThat(query.getParameters().iterator().next().getName(), is("State0"));
    }

    @Test
    public void test_addMultipleFiltersAndCreateQuery(){
        // given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ") ;
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = CustomFilter.builder(DEPLOYMENT_STATE)
                .enumType(DeploymentState.class)
                .isSelected(true)
                .build();
        filter.setValue("success");
        filters.add(filter);

        filter = CustomFilter.builder(CONFIRMATION_USER)
                .enumType(DeploymentState.class)
                .isSelected(true)
                .build();
        filter.setValue("test");
        filters.add(filter);

        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, false);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);


        // when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, Sort.builder().order(colToSort).order(uniqueCol).build(), false, false);

        // then
        assertThat(query.getParameters().size(), is(2));
    }

    @Test
    public void test_addSpecialAndJoiningFilterAndCreateQuery(){
        // given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d where d.deploymentDate = "+
                "(select max(t.deploymentDate) from DeploymentEntity t where d.context = t.context and d.resourceGroup = t.resourceGroup) ");
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = CustomFilter.builder(LASTDEPLOYJOBFORASENV)
                .isSelected(true)
                .build();
        filters.add(filter);

        filter = CustomFilter.builder(DEPLOYMENT_PARAMETER)
                .isSelected(true)
                .enumType(DeploymentState.class)
                .build();
        filter.setValue("test");
        filters.add(filter);

        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, false);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);


        // when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters,Sort.builder().order(colToSort).order(uniqueCol).build(), true, false);

        // then
        assertThat(query.getParameters().iterator().next().getName(), is("Deploymentparameter0"));
    }


    @Test
    public void test_environmentCaseInsensitiveSearchUpper(){
        //Given
        persistTestEnvironment("i");
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ");
        List<CustomFilter> filters = createEnvFilters("I");
        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, false);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);


        //when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, Sort.builder().order(colToSort).order(uniqueCol).build(), true, false);

        //then
        assertThat(query.getResultList().size(), is(1));
    }

    @Test
    public void test_environmentCaseInsensitiveSearchLower(){
        //Given
        persistTestEnvironment("X");
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ");
        List<CustomFilter> filters = createEnvFilters("x");
        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, false);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);


        //when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, Sort.builder().order(colToSort).order(uniqueCol).build(), true, false);

        //then
        assertThat(query.getResultList().size(), is(1));
    }
    @Test
    public void orderByClauseShouldContainLowerIfIgnoreCaseIsTrue(){
        //Given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ");
        List<CustomFilter> filters = createEnvFilters("x");
        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, true);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);


        //when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters,
                Sort.builder().order(colToSort).order(uniqueCol).build(),
                true, false);

        //then
        assertThat(query.unwrap(org.hibernate.query.Query.class), is(not(nullValue())));
        assertThat(query.unwrap(org.hibernate.query.Query.class).getQueryString(), containsString("LOWER(d.deploymentDate)"));

    }
    @Test
    public void orderByClauseShouldNotContainLower(){
        //Given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ");
        List<CustomFilter> filters = createEnvFilters("x");
        Sort.Order colToSort = DeploymentOrder.of("d.deploymentDate", SortingDirectionType.ASC, false);
        Sort.Order uniqueCol = DeploymentOrder.of("d.id", SortingDirectionType.DESC, false);


        //when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, Sort.builder().order(colToSort).order(uniqueCol).build(), true, false);

        //then
        assertThat(query.unwrap(org.hibernate.query.Query.class), is(not(nullValue())));
        assertThat(query.unwrap(org.hibernate.query.Query.class).getQueryString(), not(containsString("LOWER(")));

    }

    private void persistTestEnvironment(String envName){
        ContextEntity context = new ContextEntity();
        context.setName(envName);
        entityManager.persist(context);
        DeploymentEntity deployment = new DeploymentEntity();
        deployment.setContext(context);
        entityManager.persist(deployment);
    }

    private List<CustomFilter> createEnvFilters(String filterString) {
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = CustomFilter.builder(ENVIRONMENT_NAME).isSelected(true)
                .build();
        filter.setValue(filterString);
        filters.add(filter);
        return filters;
    }

}
