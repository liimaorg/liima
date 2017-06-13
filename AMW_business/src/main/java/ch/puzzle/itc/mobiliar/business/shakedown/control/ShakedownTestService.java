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

package ch.puzzle.itc.mobiliar.business.shakedown.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService;
import ch.puzzle.itc.mobiliar.business.environment.control.ContextDomainService;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Application;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ApplicationServer;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestOrder;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermissionInterceptor;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.database.control.SequencesService;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.ApplicationWithVersion;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity.DeploymentState;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.ApplicationsFromApplicationServer;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;
import ch.puzzle.itc.mobiliar.business.shakedown.event.ShakedownTestEvent;
import ch.puzzle.itc.mobiliar.business.shakedown.event.ShakedownTestEvent.ShakedownTestEventType;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.FilterType;
import ch.puzzle.itc.mobiliar.common.util.DefaultResourceTypeDefinition;
import ch.puzzle.itc.mobiliar.common.util.Tuple;

@Interceptors(HasPermissionInterceptor.class)
@Stateless
public class ShakedownTestService{

	private static final String SHAKEDOWN_TEST_QL_ALIAS = "s";
	private static final String SHAKEDOWN_ENTITY_QL = "ShakedownTestEntity";
	private static final String GROUP_QL = SHAKEDOWN_TEST_QL_ALIAS + ".resourceGroup";
	private static final String RELEASE_QL = SHAKEDOWN_TEST_QL_ALIAS + ".release";
	private static final String ENV_QL = SHAKEDOWN_TEST_QL_ALIAS + ".context";

	@Inject
	ContextDomainService context;

	@Inject
	ResourceTypeProvider resTypeProvider;

	@Inject
	ShakedownTestRunner shakedownTestRunner;

	@Inject
	private ResourceDependencyResolverService dependencyResolver;

	@Inject
	private Event<ShakedownTestEvent> shakedownTestEvent;

	@Inject
	private SequencesService sequencesService;
	
	@Inject
	private CommonFilterService commonFilterService;

	@Inject
	private EntityManager em;

	@Inject
	private Logger log;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<ResourceGroupEntity> getAllApplications(){
		return em.createNamedQuery(ResourceGroupEntity.ALLRESOURCESBYTYPE_QUERY, ResourceGroupEntity.class).setParameter("restype", DefaultResourceTypeDefinition.APPLICATION.name()).getResultList();
	}


	public enum ShakedownTestFilterTypes {
		ID("Test Id", SHAKEDOWN_TEST_QL_ALIAS + ".id", FilterType.IntegerType),
		TRACKING_ID("Test tracking Id", SHAKEDOWN_TEST_QL_ALIAS + ".trackingId", FilterType.IntegerType),
		TEST_STATE("Test state", SHAKEDOWN_TEST_QL_ALIAS + ".shakedownTestState", FilterType.StringType),
		APPSERVER_NAME("Application server", GROUP_QL + ".name", FilterType.StringType),
		APPSERVER_RELEASE("Application server release", RELEASE_QL + ".installationInProductionAt", FilterType.LabeledDateType),
		ENVIRONMENT_NAME("Environment", ENV_QL + ".name", FilterType.StringType),
		APPLICATION_NAME("Application", SHAKEDOWN_TEST_QL_ALIAS + ".appsFromAppServer", FilterType.StringType),
		TEST_DATE("Test date", SHAKEDOWN_TEST_QL_ALIAS + ".testDate", FilterType.DateType);

		private String filterDisplayName;
		private String filterTabColName;
		private FilterType filterType;

		ShakedownTestFilterTypes(String filterDisplayName, String filterTabColName, FilterType filterType) {
			this.filterDisplayName = filterDisplayName;
			this.filterTabColName = filterTabColName;
			this.filterType = filterType;
		}

		public String getFilterDisplayName() {
			return filterDisplayName;
		}

		public String getFilterTabColumnName() {
			return filterTabColName;
		}

		public FilterType getFilterType() {
			return filterType;
		}
	}

	

	public Tuple<Set<ShakedownTestEntity>, Integer> getFilteredShakedownTests(boolean doPageingCalculation, Integer startIndex, Integer maxResults,
			List<CustomFilter> filter, String colToSort, CommonFilterService.SortingDirectionType sortingDirection, List<Integer> myAMWFilter) {

		Integer totalItemsForCurrentFilter = null;
		StringBuilder stringQuery = new StringBuilder();
		String baseQuery;

		stringQuery.append("from " + SHAKEDOWN_ENTITY_QL + " " + SHAKEDOWN_TEST_QL_ALIAS + " ");

		commonFilterService.appendWhereAndMyAmwParameter(myAMWFilter, stringQuery, getEntityDependantMyAmwParameterQl());
		baseQuery = stringQuery.toString();
		
		boolean lowerSortCol = ShakedownTestFilterTypes.APPSERVER_NAME.getFilterTabColumnName().equals(colToSort);

		Query query = commonFilterService.addFilterAndCreateQuery(stringQuery, filter, colToSort, sortingDirection, SHAKEDOWN_TEST_QL_ALIAS + ".id", lowerSortCol, false);

		query = commonFilterService.setParameterToQuery(startIndex, maxResults, myAMWFilter, query);

		// some stuff may be lazy loaded
		List<ShakedownTestEntity> resultList = query.getResultList();

		Set<ShakedownTestEntity> shakedownTests = new LinkedHashSet<>();
		shakedownTests.addAll(resultList);

		if (doPageingCalculation) {
			String countQueryString = "select count(" + SHAKEDOWN_TEST_QL_ALIAS + ".id) " + baseQuery;
			Query countQuery = commonFilterService.addFilterAndCreateQuery(new StringBuilder(countQueryString), filter, null, null, null, lowerSortCol, false);

			commonFilterService.setParameterToQuery(null, null, myAMWFilter, countQuery);
			totalItemsForCurrentFilter = ((Long) countQuery.getSingleResult()).intValue();
		}

		return new Tuple<>(shakedownTests, totalItemsForCurrentFilter);

	}
	
	private String getEntityDependantMyAmwParameterQl() {
		return "(" + GROUP_QL + ".id in (:" + CommonFilterService.MY_AMW + ")) ";
	}

	@HasPermission(permission = Permission.EXECUTE_SHAKE_TEST_ORDER)
	public Integer createShakedownTestOrderReturnsTrackingId(List<ShakedownTestOrder> shakedownTestOrder) {
		Integer orderTrackingId = null;
		if (shakedownTestOrder != null) {

			orderTrackingId = sequencesService.getNextValueAndUpdate(ShakedownTestEntity.SEQ_NAME);

			for (ShakedownTestOrder orderShakedownTest : shakedownTestOrder) {

				ContextEntity contextEntity = em.find(ContextEntity.class, orderShakedownTest.getEnvironment().getId());

				for (ResourceGroupEntity appServerGroup : orderShakedownTest.getResourceGroups()) {
					ResourceGroupEntity group = em.find(ResourceGroupEntity.class, appServerGroup.getId());
					ResourceEntity appServerEntity;
					DeploymentEntity deploymentForAs;
					ReleaseEntity selectedRelease;

					if (orderShakedownTest.getRelease() != null) {
						// find appServer by selected release
						selectedRelease = orderShakedownTest.getRelease();
						appServerEntity = dependencyResolver.getResourceEntityForRelease(group, selectedRelease);
						deploymentForAs = getLastSuccessDeploymentEntityForGroupReleaseAndContext(group, appServerEntity.getRelease(), contextEntity);
					} else {
						deploymentForAs = getLastSuccessDeploymentEntityForGroupAndContext(group, contextEntity);
						appServerEntity = deploymentForAs != null ? deploymentForAs.getResource() : null;
						selectedRelease = deploymentForAs != null ? deploymentForAs.getRelease() : null;
					}

					if (appServerEntity != null) {
						ApplicationServer.createByResource(appServerEntity, resTypeProvider, null);

						Map<Integer, String> appIdWithAppNameMap = new HashMap<>();
					     Set<ResourceEntity> applications = dependencyResolver.getConsumedRelatedResourcesByResourceType(appServerEntity, DefaultResourceTypeDefinition.APPLICATION, selectedRelease);
						for (ResourceEntity application : applications) {
							appIdWithAppNameMap.put(application.getId(), application.getName());
						}

						createShakedownEntity(orderTrackingId, contextEntity, appServerEntity, deploymentForAs, appIdWithAppNameMap, selectedRelease, group, new Date());

						if (orderShakedownTest.isCreateTestForNeighbourhood()) {
							createShakedownEntityForProviderAsForAllConsumedResources(appServerEntity, orderTrackingId, contextEntity, selectedRelease);
						}
					}
					else {
						log.log(Level.WARNING, "Could not find an application server '" + group.getName() + "' in release "
								+ orderShakedownTest.getRelease().getName());
					}
				}
			}
		}
		return orderTrackingId;
	}

	//TODO: move to DeploymentBoundary
	private DeploymentEntity getLastSuccessDeploymentEntityForGroupReleaseAndContext(ResourceGroupEntity resourceGroup, ReleaseEntity releaseEntity, ContextEntity contextEntity) {	
		Query query = em
				.createQuery("from DeploymentEntity d where d.resourceGroup=:resourceGroup and d.context=:contextEntity and d.release=:releaseEntity and d.deploymentState is :successState order by d.deploymentDate desc");
		query.setParameter("resourceGroup", resourceGroup);
		query.setParameter("contextEntity", contextEntity);
		query.setParameter("releaseEntity", releaseEntity);
		query.setParameter("successState", DeploymentEntity.DeploymentState.success);

		List<DeploymentEntity> result = query.getResultList();

		if (result == null || result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}
	
	//TODO: move to DeploymentBoundary
	private DeploymentEntity getLastSuccessDeploymentEntityForGroupAndContext(ResourceGroupEntity resourceGroup, ContextEntity contextEntity) {

		TypedQuery<DeploymentEntity> query = em
				.createQuery("from DeploymentEntity d where d.resourceGroup=:resourceGroup and d.context=:contextEntity and d.deploymentState is :successState order by d.deploymentDate desc", DeploymentEntity.class);
		query.setParameter("resourceGroup", resourceGroup);
		query.setParameter("contextEntity", contextEntity);
		query.setParameter("successState", DeploymentEntity.DeploymentState.success);

		List<DeploymentEntity> result = query.getResultList();

		if (result == null || result.isEmpty()) {
			return null;
		}
		return result.get(0);
	}


	/**
	 * Create and execute shakedowntests for all deploymentes in list if deployment was successfull.
	 * 
	 * @param deployments
	 */
	public void createAndExecuteShakedowntestForDeployments(List<DeploymentEntity> deployments) {
		for (DeploymentEntity deployment : deployments) {
			if (DeploymentState.success.equals(deployment.getDeploymentState())) {
				createShakedownTestOrderForDeploymentOrder(deployment);
			}
		}

		// fire the Event to execute the Shakedowntests
		shakedownTestEvent.fire(new ShakedownTestEvent(ShakedownTestEventType.NEW, shakedownTest_state.scheduled));
	}

	// TODO überprüfung: permissions für automatische testerstellung bei
	// deployments!!!
	protected void createShakedownTestOrderForDeploymentOrder(DeploymentEntity deployment) {

		ResourceEntity appServerEntity = dependencyResolver.getResourceEntityForRelease(deployment.getResourceGroup(), deployment.getRelease());
		// TODO: get correct appServer from history?

		if (appServerEntity != null) {
			ContextEntity contextEntity = deployment.getContext();

			List<ApplicationWithVersion> applicationsWithVersion = deployment.getApplicationsWithVersion();
			Map<Integer, String> appIdWithAppNameMap = new HashMap<>();
			for (ApplicationWithVersion applicationWithVersion : applicationsWithVersion) {
				appIdWithAppNameMap.put(applicationWithVersion.getApplicationId(), applicationWithVersion.getApplicationName());
			}

			createShakedownEntity(deployment.getTrackingId(), contextEntity, appServerEntity, deployment, appIdWithAppNameMap, deployment.getRelease(),
					deployment.getResourceGroup(),
					deployment.getDeploymentDate());

			if (deployment.isCreateTestForNeighborhoodAfterDeployment()) {
				createShakedownEntityForProviderAsForAllConsumedResources(appServerEntity, deployment.getTrackingId(), contextEntity, deployment.getRelease());
			}
		} else {
			log.log(Level.WARNING, "Could not find an application server '" + deployment.getResourceGroup().getName() + "' in release " + deployment.getRelease().getName());
		}
	}

	private void createShakedownEntityForProviderAsForAllConsumedResources(ResourceEntity appServerEntity, Integer orderTrackingId,
			ContextEntity contextEntity, ReleaseEntity releaseEntity) {
		ApplicationServer as = ApplicationServer.createByResource(appServerEntity, resTypeProvider, context.getGlobalResourceContextEntity());
		Date testDate = new Date();
		for (Application a : as.getApplications()) {
			for (Resource r : a.getRelatedResources()) {
				for (ProvidedResourceRelationEntity rel : r.getEntity().getProvidedSlaveRelations()) {
					ResourceEntity providingApp = rel.getMasterResource();
					for (ConsumedResourceRelationEntity asToApp : providingApp.getConsumedSlaveRelations()) {
						ResourceEntity providingASEntity = asToApp.getMasterResource();
						ApplicationServer.createByResource(providingASEntity, resTypeProvider,
								context.getGlobalResourceContextEntity());

						Map<Integer, String> appIdWithAppNameMap = new HashMap<>();
					     Set<ResourceEntity> applications = dependencyResolver.getConsumedRelatedResourcesByResourceType(providingASEntity, DefaultResourceTypeDefinition.APPLICATION, releaseEntity);

					    	for (ResourceEntity application : applications) {
							appIdWithAppNameMap.put(application.getId(), application.getName());
						}

						DeploymentEntity deploymentForAs = getLastSuccessDeploymentEntityForGroupAndContext(providingASEntity.getResourceGroup(),
								contextEntity);

						createShakedownEntity(orderTrackingId, contextEntity, providingASEntity, deploymentForAs, appIdWithAppNameMap, appServerEntity.getRelease(),
								providingASEntity.getResourceGroup(), testDate);
					}
				}
			}
		}
	}

	private boolean hasNoShakedownTestForASAndContextWithinCurrentOrder(Integer orderTrackingId, ContextEntity contextEntity,
			ResourceEntity providingASEntity) {

		TypedQuery<ShakedownTestEntity> q = em.createQuery("from " + SHAKEDOWN_ENTITY_QL
				+ " test where test.trackingId=:trackingId and test.applicationServer=:appServer and test.context=:context",ShakedownTestEntity.class);
		q.setParameter("trackingId", orderTrackingId);
		q.setParameter("appServer", providingASEntity);
		q.setParameter("context", contextEntity);

		List<ShakedownTestEntity> result = q.getResultList();

		return result == null || result.isEmpty();
	}

	private void createShakedownEntity(Integer orderTrackingId, ContextEntity contextEntity, ResourceEntity appServerEntity, DeploymentEntity deploymentForAs,
			Map<Integer, String> applicationIdWithNameMap, ReleaseEntity release, ResourceGroupEntity resourceGroup, Date testDate) {

		if (hasNoShakedownTestForASAndContextWithinCurrentOrder(orderTrackingId, contextEntity, appServerEntity)) {

			ShakedownTestEntity shakedownTest = new ShakedownTestEntity();
			shakedownTest.setTrackingId(orderTrackingId);
			shakedownTest.setContext(contextEntity);
			shakedownTest.setApplicationServer(appServerEntity);
			shakedownTest.setRelease(release);
			shakedownTest.setResourceGroup(resourceGroup);
			shakedownTest.setTestDate(testDate);

			List<ApplicationsFromApplicationServer> appList = new ArrayList<>();

			for (Integer applicationId : applicationIdWithNameMap.keySet()) {
				appList.add(new ApplicationsFromApplicationServer(applicationIdWithNameMap.get(applicationId), applicationId));
			}

			shakedownTest.setApplicationsFromApplicationServer(appList);

			shakedownTest.setDeployment(deploymentForAs);

			em.persist(shakedownTest);
			em.flush();
		}
	}

	/**
	 * Returns all ShakedownTestEntity which are not yet executed
	 * 
	 * @return
	 */
	public List<ShakedownTestEntity> getTestsToExecute() {
		try {
			return em.createQuery("from " + SHAKEDOWN_ENTITY_QL + " test where test.isExecuted=false", ShakedownTestEntity.class).getResultList();
		}
		catch (Exception e) {
			log.warning("Konnte ShakedownTests nicht von der DB auslesen - ein Grund ist womöglich ein Locking eines anderen Servers");
			return new ArrayList<>();
		}
	}

	/**
	 * Updates the test execution time to "now"
	 * 
	 * @param deployment
	 *             - the deployment entity to be updated
	 */
	private void setTestExecutedTime(final ShakedownTestEntity test) {
		test.setTestDate(new Date());
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public ShakedownTestEntity updateShakedownInfo(final Integer shakedownId, final String testResult, shakedownTest_state state) {

		ShakedownTestEntity shakedownTest = em.find(ShakedownTestEntity.class, shakedownId);
		// The shakedowntest could be stale in the meantime because of the
		// asynchronous update - better refresh from db
		em.refresh(shakedownTest);

		setTestExecutedTime(shakedownTest);
		shakedownTest.setTestResult(testResult);
		shakedownTest.setShakedownTestStateDisplayName(state);

		em.merge(shakedownTest);
		return shakedownTest;
	}

	/**
	 * Returns the ShakedownTest by the given Id
	 * 
	 * @param shakeDownTestId
	 */
	public ShakedownTestEntity getShakedownTestById(Integer shakeDownTestId) {
		Query query = em.createQuery("from " + SHAKEDOWN_ENTITY_QL + " test where test.id=:id").setParameter("id", shakeDownTestId);
		return (ShakedownTestEntity) query.getSingleResult();
	}

	public List<ShakedownTestEntity> getShakedownTestsByTrackingId(Integer trackingId) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<ShakedownTestEntity> q = cb.createQuery(ShakedownTestEntity.class);
		Root<ShakedownTestEntity> r = q.from(ShakedownTestEntity.class);
		Predicate trackingIdPred = cb.equal(r.<String> get("trackingId"), trackingId);
		q.where(trackingIdPred);

		return em.createQuery(q).getResultList();
	}

}
