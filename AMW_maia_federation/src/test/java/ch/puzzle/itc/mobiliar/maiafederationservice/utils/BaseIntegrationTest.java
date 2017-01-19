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

package ch.puzzle.itc.mobiliar.maiafederationservice.utils;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.datatype.common.commons.v3.CallContext;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;
import ch.puzzle.itc.mobiliar.business.database.control.EntityManagerProducerIntegrationTestImpl;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.foreignable.entity.ForeignableOwner;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyDescriptorService;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyValueService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.CopyResourceDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceRepository;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeDomainService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceTypeEntity;
import ch.puzzle.itc.mobiliar.business.security.control.PermissionService;
import ch.puzzle.itc.mobiliar.business.softlinkRelation.control.SoftlinkRelationService;
import ch.puzzle.itc.mobiliar.common.util.ConfigurationService;
import ch.puzzle.itc.mobiliar.maiafederationservice.boundary.MaiaAmwFederationServiceApplicationBean;
import ch.puzzle.itc.mobiliar.test.CustomLogging;
import ch.puzzle.itc.mobiliar.test.testrunner.WeldJUnit4Runner;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hibernate.Session;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

@RunWith(WeldJUnit4Runner.class)
public abstract class BaseIntegrationTest {

		protected static final String CALLCONTEXT_CALLER = "callcontext_caller";
		protected static final String CALLCONTEXT_USER = "callcontext_user";
		protected static final String CALLCONTEXT_UUID = "callcontext_uuid";

		protected static final String REQUEST_USER_MAIA = ForeignableOwner.MAIA.name();
		protected static final CallContext CALLCONTEXT = new CallContext(CALLCONTEXT_CALLER,
				CALLCONTEXT_USER, CALLCONTEXT_UUID);

		protected static final String[] PPI_TYPES = { "ws-tokyo-ppi-fake" };
		protected static final String[] CPI_TYPES = { "ws-tokyo-fake", "wstokyo" };

		protected static final String PAST_RELEASE = "Past";
		protected static final String MAIN_RELEASE_14_10 = "RL-14.10";
		protected static final String MAIN_RELEASE_16_04 = "RL-16.04";
		protected static final String MAIN_RELEASE_16_10 = "RL-16.10";
		protected static final String MAIN_RELEASE_17_04 = "RL-17.04";

		@Inject
		protected EntityManager entityManager;

		@Inject
        protected ResourceRepository resourceRepository;

		@Inject
		private PermissionService permissionService;

		@Inject
		protected SoftlinkRelationService softlinkRelationService;

		@Inject
		protected XMLDataReaderConverter xmlDataReaderConverter;

		@Inject
		protected ResourceTypeDomainService resourceTypeService;

		@Inject
		protected ContextDomainService contextService;

		@Inject
		protected PropertyDescriptorService descriptorService;

		@Inject
		ReleaseMgmtService releaseService;

		@Inject
		protected MaiaAmwFederationServiceApplicationBean maiaAmwFederationServiceApplicationBean;

		@Inject
		protected CopyResourceDomainService copyResourceDomainService;

		@Inject
		protected PropertyValueService propertyValueService;

		@Inject
		protected CopyResource copyResourceService;

		protected Map<String, ReleaseEntity> addedReleaseEntitiesCache = new HashMap<>();

		protected void setUp() {

			entityManager.getTransaction().begin();
			try {
				Connection connection = ((SessionFactoryImpl)
                        entityManager.unwrap(Session.class).getSessionFactory()).getConnectionProvider().getConnection();

				Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

				Liquibase liquibase = new Liquibase("integration-test/data/testdata.xml", new ClassLoaderResourceAccessor(), database);
				liquibase.update(new Contexts(), new LabelExpression());
			} catch (SQLException |  LiquibaseException e) {
				throw new RuntimeException("Error loading testdata", e);
			}

			entityManager.getTransaction().commit();
			entityManager.getTransaction().begin();

			System.getProperties().put(ConfigurationService.ConfigKey.LOGS_PATH.getValue(),
					"/tmp/integration-test/log");

			new CustomLogging().setLevel(Level.WARNING);
		}

		protected void tearDown() {
			entityManager.getTransaction().rollback();

			// remove the systemproperties to not have any sideeffect on other tests
			System.getProperties().remove(ConfigurationService.ConfigKey.LOGS_PATH.getValue());
		}

		protected UpdateRequest getUpdateRequestFor(String xmlFileName) {
			UpdateRequest updateRequestFromUsecaseFile = xmlDataReaderConverter.getUpdateRequestFromUsecaseFile(xmlFileName);
			return updateRequestFromUsecaseFile;
		}

		protected List<ResourceEntity> getResourceByName(String resourceName) {
            // filter out duplicates !
            SortedSet<ResourceEntity> singleResources = new TreeSet<>(resourceRepository.getResourcesByGroupNameWithAllRelationsOrderedByRelease(resourceName));
            return new ArrayList<>(singleResources);
		}

		protected ResourceEntity getResourceByNameAndRelease(String resourceName, String releaseName) {
            ReleaseEntity release = findReleaseByName(releaseName);
			List<ResourceEntity> resources = getResourceByName(resourceName);
			for(ResourceEntity resource : resources){
				if (release.getId().equals(resource.getRelease().getId())){
                    return resource;
                }
            }

            throw new NoResultException("No resource for release");
		}

		@Deprecated // must be removed as soon the data are delivered with correct types
		protected void addResourceTypes() {
			for (String ppiType : PPI_TYPES) {
				ResourceTypeEntity resourceType = new ResourceTypeEntity();
				resourceType.setName(ppiType);
				resourceType.setParentResourceType(entityManager.find(ResourceTypeEntity.class,
							resourceTypeService.getResourceTypeIdByResourceTypeName("TokyoWSPPI")));
				entityManager.persist(resourceType);
			}
			for (String cpiType : CPI_TYPES) {
				ResourceTypeEntity resourceType = new ResourceTypeEntity();
				resourceType.setName(cpiType);
				resourceType.setParentResourceType(entityManager.find(ResourceTypeEntity.class,
							resourceTypeService.getResourceTypeIdByResourceTypeName("TokyoWSCPI")));
				entityManager.persist(resourceType);
			}
		}

		protected void addReleases() {
			createAndAddPastRelease();
			createAndAddMainRelease14_10();
			createAndAddMainRelease16_04();
			createAndAddMainRelease16_10();
			createAndAddMainRelease17_04();
		}

		protected void createAndAddPastRelease() {
			createAndAddReleaseIfNotYetExist(PAST_RELEASE, new Date(2007, Calendar.APRIL, 1), true);
		}

		protected void createAndAddMainRelease14_10() {
			createAndAddReleaseIfNotYetExist(MAIN_RELEASE_14_10, new Date(2014, Calendar.OCTOBER, 1), true);
		}

		protected void createAndAddMainRelease16_04() {
			createAndAddReleaseIfNotYetExist(MAIN_RELEASE_16_04, new Date(2016, Calendar.APRIL, 1), true);
		}

		protected void createAndAddMainRelease16_10() {
			createAndAddReleaseIfNotYetExist(MAIN_RELEASE_16_10, new Date(2016, Calendar.OCTOBER, 1), true);
		}

		protected void createAndAddMainRelease17_04() {
			createAndAddReleaseIfNotYetExist(MAIN_RELEASE_17_04, new Date(2017, Calendar.APRIL, 1), true);
		}

		protected void createAndAddReleaseIfNotYetExist(String releaseName, Date date,
				boolean isMainRelease) {
			ReleaseEntity release = findReleaseByName(releaseName);
			if (release == null) {
				release = new ReleaseEntity();
				release.setName(releaseName);
				release.setInstallationInProductionAt(date);
				release.setMainRelease(isMainRelease);
				entityManager.persist(release);
			}
			addedReleaseEntitiesCache.put(releaseName, release);
		}

		/**
		 * First read cache map if already loaded otherwise get from db and add to cache
		 * 
		 * @param releaseName
		 * @return
		 */
		protected ReleaseEntity findReleaseByName(String releaseName) {
			ReleaseEntity releaseEntity = addedReleaseEntitiesCache.get(releaseName);
			if (releaseEntity == null) {
				releaseEntity = releaseService.findByName(releaseName);
				addedReleaseEntitiesCache.put(releaseName, releaseEntity);
			}
			return releaseEntity;
		}

		protected UpdateResponse doUpdate(String fileName) throws  BusinessException, TechnicalException, ValidationException {
			UpdateRequest updateRequest = getUpdateRequestFor(fileName);

			// when
			return maiaAmwFederationServiceApplicationBean.update(CALLCONTEXT, REQUEST_USER_MAIA, updateRequest);
		}

}
