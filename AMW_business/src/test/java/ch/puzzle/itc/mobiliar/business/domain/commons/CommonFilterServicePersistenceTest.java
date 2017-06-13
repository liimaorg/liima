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

import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.test.testrunner.PersistenceTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(PersistenceTestRunner.class)
public class CommonFilterServicePersistenceTest {

	@Spy
	@PersistenceContext
	EntityManager entityManager;

	@Mock
	Logger log;

	@InjectMocks
	CommonFilterService service;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}


    @Test
    public void test_addFilterAndCreateQuery(){
        // given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ") ;
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = new CustomFilter("State", "d.deploymentState", CustomFilter.FilterType.ENUM_TYPE);
        filter.setValue("success");
        filter.setEnumType(DeploymentEntity.DeploymentState.class);
        filter.setComperatorSelection(CustomFilter.ComperatorFilterOption.equals);
        filter.setSelected(true);
        filters.add(filter);
        String colToSort = "d.deploymentDate";
        String uniqueCol ="d.id";

        // when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, colToSort, CommonFilterService.SortingDirectionType.ASC,uniqueCol, false, false);

        // then
        assertThat(query.getParameters().size(), is(1));
        assertThat(query.getParameters().iterator().next().getName(), is("State0"));
    }

    @Test
    public void test_addMultipleFiltersAndCreateQuery(){
        // given
        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d ") ;
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = new CustomFilter("State", "d.deploymentState", CustomFilter.FilterType.ENUM_TYPE);
        filter.setValue("success");
        filter.setEnumType(DeploymentEntity.DeploymentState.class);
        filter.setComperatorSelection(CustomFilter.ComperatorFilterOption.equals);
        filter.setSelected(true);
        filters.add(filter);
        filter = new CustomFilter("Deployment parameter", "p.key", "join d.deploymentParameters p", CustomFilter.FilterType.StringType);
        filter.setValue("test");
        filter.setEnumType(DeploymentEntity.DeploymentState.class);
        filter.setComperatorSelection(CustomFilter.ComperatorFilterOption.equals);
        filter.setSelected(true);
        filters.add(filter);

        String colToSort = "d.deploymentDate";
        String uniqueCol ="d.id";

        // when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, colToSort, CommonFilterService.SortingDirectionType.ASC,uniqueCol, false, false);

        // then
        assertThat(query.getParameters().size(), is(2));
    }

    @Test
    public void test_addSpecialAndJoiningFilterAndCreateQuery(){
        // given

        StringBuilder stringQuery = new StringBuilder("select d from DeploymentEntity d where d.deploymentDate = "+
                "(select max(t.deploymentDate) from DeploymentEntity t where d.context = t.context and d.resourceGroup = t.resourceGroup) ");
        List<CustomFilter> filters = new ArrayList<>();
        CustomFilter filter = new CustomFilter("Latest deployment job for App Server and Env", "", CustomFilter.FilterType.SpecialFilterType);
        filter.setSelected(true);
        filters.add(filter);

        filter = new CustomFilter("Deployment parameter", "p.key", "join d.deploymentParameters p", CustomFilter.FilterType.StringType);
        filter.setValue("test");
        filter.setEnumType(DeploymentEntity.DeploymentState.class);
        filter.setComperatorSelection(CustomFilter.ComperatorFilterOption.equals);
        filter.setSelected(true);
        filters.add(filter);

        String colToSort = "d.deploymentDate";
        String uniqueCol ="d.id";

        // when
        Query query = service.addFilterAndCreateQuery(stringQuery, filters, colToSort, CommonFilterService.SortingDirectionType.ASC,uniqueCol, false, true);

        // then
        assertThat(query.getParameters().iterator().next().getName(), is("Deploymentparameter0"));
    }

}
