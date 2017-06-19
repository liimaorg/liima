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

package ch.puzzle.itc.mobiliar.presentation.shakedowntest;

import ch.puzzle.itc.mobiliar.business.deploy.entity.CustomFilter;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentEntity;
import ch.puzzle.itc.mobiliar.business.deploy.entity.DeploymentFilterTypes;
import ch.puzzle.itc.mobiliar.business.domain.commons.CommonFilterService.SortingDirectionType;
import ch.puzzle.itc.mobiliar.business.environment.entity.ContextEntity;
import ch.puzzle.itc.mobiliar.business.generator.control.extracted.ResourceDependencyResolverService;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.control.ResourceTypeProvider;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestEntity.shakedownTest_state;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestFilterTypes;
import ch.puzzle.itc.mobiliar.business.shakedown.entity.ShakedownTestOrder;
import ch.puzzle.itc.mobiliar.common.exception.NotAuthorizedException;
import ch.puzzle.itc.mobiliar.common.util.Tuple;
import ch.puzzle.itc.mobiliar.presentation.common.ContextDataProvider;
import ch.puzzle.itc.mobiliar.presentation.components.impl.CustomFilterComp;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;
import lombok.Getter;
import lombok.Setter;

import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

@Named
@SessionScoped // TODO: Migrate to new concept Be careful state of filter not kept in viewScoped
public class ShakedownTestScreenDataProvider implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String EMPTY_STRING = "";

	private enum PresetViewOptions {
		SHAKEDOWN_TEST("Shakedown tests");
		private String displayName;

		private PresetViewOptions(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return this.displayName;
		}
	}

	/**
	 * Falls enum angepasst oder refactored wird, so muss dies zwingend im shakedowntest.xhtml nachgetragen
	 * werden!
	 */
	public enum shakedownTestScreenColDescriptor {
		ID, TRACKING_ID, STATE, APPSERVER_NAME, RELEASE, ENV, TESTDATE
	}

	@Inject
	ReleaseMgmtService releaseMgmtService;

	@Inject
	ResourceDependencyResolverService dependencyResolverService;

	@Inject
	ShakedownTestScreenController controller;

	@Inject
	ContextDataProvider contextDataProvider;

	@Inject
	ResourceTypeProvider resourceTypeProvider;

	private List<ShakedownTestEntity> shakedownTests;
	private List<String> shakedowntestPresetViewsOptions;
	private String selectedShakedownTestPresetView = PresetViewOptions.SHAKEDOWN_TEST.getDisplayName();
	@Getter
	@Setter
	private Integer selectedShakedownTestId;
	private int countAllShakedownTests;
	private ArrayList<String> allAppDisplayNames;
	private Map<Integer, ResourceGroupEntity> asGroups;
	private Map<Integer, List<ResourceGroupEntity>> contextIdMapWithSuccessfullLastDeployedAppServer;
	@Getter
	@Setter
	private Integer selectedOrderEnvironmentId;
	@Getter
	@Setter
	private Integer selectedOrderAppServerGroupId;
	private List<ShakedownTestOrder> shakedownTestOrder;
	@Getter
	@Setter
	private Integer trackingIdFromLastCreatedOrder;
	@Getter
	@Setter
	private boolean showOnlyDeployedAppServers;

	// paging, sorting
	@Getter
	private int currentPage;
	private int itemsPerPage;
	private SortingDirectionType sortingDirection = null;
	private shakedownTestScreenColDescriptor sortingColumn = null;

	// Releasing
	@Getter
	private ReleaseEntity selectedRelease;
	@Getter
	private List<ReleaseEntity> allReleases;
	private Map<Integer, ReleaseEntity> allReleasesMap;

	@Getter
	private boolean renderCreateOrderDialog;

	private ShakedownTestFilterTypes selectedFilter;

	@Getter
	private CustomFilterComp customFilterComp = new CustomFilterCompImpl();


	public void initView() {
	    if (!FacesContext.getCurrentInstance().isPostback()) {
		   reloadShakedownTests(true);
		   initDefaultSelection();
		   loadAllReleases();
		   loadApplicationServerGroups();
		   showOnlyDeployedAppServers = false;
		   selectedOrderAppServerGroupId = null;
		   selectedRelease = null;
	    }
	}

	private class CustomFilterCompImpl extends CustomFilterComp {


		@Override
		public void addSelectedFilter() {

			selectedFilter = getSelectedEnumType(getSelectedFilterItemEnumName());

			if (selectedFilter != null) {
				CustomFilter filter = CustomFilter.builder(selectedFilter).build();
				filter.setComparatorSelection(filter.getTypedComperatorSelectionList().isEmpty() ? null : filter.getTypedComperatorSelectionList().get(0));
				if (selectedFilter.equals(DeploymentFilterTypes.LASTDEPLOYJOBFORASENV) && hasAlreadySpecialTypeFilter(getSelectedFilterList())) {
					GlobalMessageAppender.addErrorMessage("This filter is already set.");
				} else {
					filter = setFilterDropDownOptions(filter);

					getSelectedFilterList().add(filter);

				}
			}
			selectedFilter = null;

		}

		private ShakedownTestFilterTypes getSelectedEnumType(String selectedFilter) {
			ShakedownTestFilterTypes result = null;
			for (ShakedownTestFilterTypes filterType : ShakedownTestFilterTypes.values()) {
				if (filterType.name().equals(selectedFilter)) {
					result = filterType;
					break;
				}
			}

			return result;
		}

		@Override
		protected void loadFilterList() {
			List<FilterSelectionItem> filterSelectionItemList = new ArrayList<CustomFilterComp.FilterSelectionItem>();
			for (ShakedownTestFilterTypes filterType : ShakedownTestFilterTypes.values()) {
				filterSelectionItemList.add(new FilterSelectionItem(filterType.name(), filterType
						.getFilterDisplayName()));
			}
			setFilterSelectionList(filterSelectionItemList);
		}

		@Override
		protected CustomFilter setFilterDropDownOptions(CustomFilter filter) {

			if (filter.getFilterDisplayName().equals(
					ShakedownTestFilterTypes.APPSERVER_NAME.getFilterDisplayName())) {
				filter.setDropDownItems(getAllAppServerNames());
			}

			if (filter.getFilterDisplayName().equals(
					ShakedownTestFilterTypes.ENVIRONMENT_NAME.getFilterDisplayName())) {
				filter.setDropDownItems(getAllEnvNames());
			}

			if (filter.getFilterDisplayName().equals(
					ShakedownTestFilterTypes.APPLICATION_NAME.getFilterDisplayName())) {
				filter.setDropDownItems(getAllAppDisplayNames());
			}

			if (filter.getFilterDisplayName().equals(
					ShakedownTestFilterTypes.TEST_STATE.getFilterDisplayName())) {
				filter.setDropDownItems(getAllStates());
			}
			if (filter.getFilterDisplayName().equals(
					ShakedownTestFilterTypes.APPSERVER_RELEASE.getFilterDisplayName())) {
				Map<String, String> releaseMap = new LinkedHashMap<>();
				for (ReleaseEntity r : allReleases) {
					releaseMap.put(r.getName(),
							CustomFilter.convertDateToString(r.getInstallationInProductionAt()));
				}
				filter.setDropDownItemsMap(releaseMap);
			}
			return filter;
		}

	}

	public void addShakedownTestOrder() {
		boolean valid = true;
		if (selectedOrderEnvironmentId == null || selectedOrderEnvironmentId == 0) {
			GlobalMessageAppender.addErrorMessage("No environment selected");
			valid = false;
		}
		if (selectedOrderAppServerGroupId == null || selectedOrderAppServerGroupId == 0) {
			GlobalMessageAppender.addErrorMessage("No application server selected");
			valid = false;
		}
		if (selectedRelease == null && !showOnlyDeployedAppServers) {
			GlobalMessageAppender.addErrorMessage("No release selected");
			valid = false;
		}

		if (valid) {
			List<ResourceGroupEntity> selectedAppServerGroups = null;
            ContextEntity context = getContextForId(selectedOrderEnvironmentId);

			if (selectedOrderAppServerGroupId > 0) {
				selectedAppServerGroups = Collections.singletonList(asGroups
						.get(selectedOrderAppServerGroupId));
			}
			else if (showOnlyDeployedAppServers) {
				// deployed appServers
				selectedAppServerGroups = new ArrayList<ResourceGroupEntity>(getDeployedAppServerGroups());
			}
			else {
				// all appServers
				selectedAppServerGroups = new ArrayList<ResourceGroupEntity>(asGroups.values());
			}

			ShakedownTestOrder shakedownTestOrder = new ShakedownTestOrder(context, selectedRelease, selectedAppServerGroups);
			getShakedownTestOrder().add(shakedownTestOrder);
		}
	}


	
	private ContextEntity getContextForId(Integer id) {
		for (ContextEntity context : contextDataProvider.getEnvironments()) {
			if (context.getId().equals(id)) {
				return context;
			}
		}
		return null;
	}

	public List<ResourceGroupEntity> getAppServerGroups() {
		if (showOnlyDeployedAppServers) {
			return getDeployedAppServerGroups();
		}
		List<ResourceGroupEntity> result = new ArrayList<ResourceGroupEntity>(asGroups.values());
		Collections.sort(result);
		return result;
	}

	private List<ResourceGroupEntity> getDeployedAppServerGroups() {
		if (selectedOrderEnvironmentId != null && selectedOrderEnvironmentId != null) {
			List<ResourceGroupEntity> asList = contextIdMapWithSuccessfullLastDeployedAppServer
					.get(selectedOrderEnvironmentId);
			if (asList != null) {
				Collections.sort(asList);
				return asList;
			}
		}
		return Collections.emptyList();
	}

	public void createShakedownTest() {
		try {

			trackingIdFromLastCreatedOrder = controller.createShakedownTestOrder(getShakedownTestOrder());

			if (trackingIdFromLastCreatedOrder != null) {

				setFilterForLastShakedownTestOrder();
				getShakedownTestOrder().clear();
				reloadShakedownTests(true);
			}
		}
		catch (EJBException e) {
			if (e.getCause() instanceof NotAuthorizedException) {
				GlobalMessageAppender.addErrorMessage(e.getCause().getMessage());
			}
			else {
				throw e;
			}
		}
	}

	public void removeSelectedShakedownTestFromOrder(ShakedownTestOrder test) {
		if (getShakedownTestOrder().contains(test)) {
			getShakedownTestOrder().remove(test);
		}
	}

	public List<ShakedownTestOrder> getShakedownTestOrder() {
		if (shakedownTestOrder == null) {
			shakedownTestOrder = new ArrayList<ShakedownTestOrder>();
		}
		return shakedownTestOrder;
	}

	private void setFilterForLastShakedownTestOrder() {

		if (trackingIdFromLastCreatedOrder != null) {

			getCustomFilterComp().removeAllFilter();
			CustomFilter filter = CustomFilter.builder(ShakedownTestFilterTypes.TRACKING_ID).build();
			filter.setValue(trackingIdFromLastCreatedOrder.toString());

			getCustomFilterComp().getSelectedFilterList().add(filter);

			sortingDirection = SortingDirectionType.ASC;
			sortingColumn = shakedownTestScreenColDescriptor.ID;
		}

	}

	public void goToScreen(Integer screen) {
		currentPage = Math.min(Math.max(0, screen), maxScreens() - 1);
		reloadShakedownTests(false);
	}

	public void nextScreen() {
		currentPage = Math.min(++currentPage, maxScreens() - 1);
		reloadShakedownTests(false);
	}

	public void lastScreen() {
		currentPage = maxScreens() - 1;
		reloadShakedownTests(false);
	}

	public void changeSortOrderForSelectedColumn(String enumName) {
		shakedownTestScreenColDescriptor col = getEnumValueForString(enumName);
		if (col != null) {
			if (sortingColumn.equals(col)) {
				if (sortingDirection == null) {
					// set to asc
					sortingDirection = SortingDirectionType.ASC;
				}
				else {
					switch (sortingDirection) {
					case ASC:
						// set to desc
						sortingDirection = SortingDirectionType.DESC;
						break;
					case DESC:
						// set to asc
						sortingDirection = SortingDirectionType.ASC;
						break;
					}
				}
			}
			else {
				// auswahl einer neuen spalte beginnt immer mit asc sortierung
				sortingColumn = col;
				sortingDirection = SortingDirectionType.ASC;
			}
		}
		// do sorting
		reloadShakedownTests(false);
	}

	public void shakedownTestPresetViewChangeListener(String selectedShakedownTestPresetView) {
		if (!this.selectedShakedownTestPresetView.equals(selectedShakedownTestPresetView)) {
			this.selectedShakedownTestPresetView = selectedShakedownTestPresetView;
			reloadShakedownTests(false);
		}
	}

	public void firstScreen() {
		currentPage = 0;
		reloadShakedownTests(false);
	};

	public void previousScreen() {
		currentPage = Math.max(0, --currentPage);
		reloadShakedownTests(false);
	}

	public void setItemsPerPage(int itemsPerPage) {
		if (itemsPerPage < 0) {
			itemsPerPage = 0;
		}
		if (itemsPerPage != this.itemsPerPage) {
			this.itemsPerPage = itemsPerPage;
			this.currentPage = 0;
			reloadShakedownTests(false);
		}
	}



	public void reloadShakedownTests(boolean countAgain) {
		if (countAgain) {
			currentPage = 0;
		}
		Tuple<Set<ShakedownTestEntity>, Integer> result = controller.loadPendingShakedownTests(countAgain,
				currentStartIndex(), getItemsPerPage(), getAppliedFilterList(), sortingColumn,
				sortingDirection);
		if (result.getB() != null) {
			countAllShakedownTests = result.getB();
		}

		shakedownTests = new ArrayList<ShakedownTestEntity>(result.getA());
	}

	public List<CustomFilter> getAppliedFilterList() {

		List<CustomFilter> appliedFilter = new ArrayList<CustomFilter>();

		if (isShakedownTestViewSelected()) {

			for (CustomFilter filter : getCustomFilterComp().getSelectedFilterList()) {
				if (filter.isSelected()) {
					appliedFilter.add(filter);
				}
			}
		}

		return appliedFilter;
	}

	private int currentStartIndex() {
		return currentPage * getItemsPerPage();
	}

	public List<Integer> availablePages() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(0);
		for (int i = 1; i < maxScreens(); i++) {
			list.add(i);
		}
		return list;
	}

	public boolean isLastPage() {
		return currentPage >= maxScreens() - 1;
	}

	private int maxScreens() {
		return (int) Math.ceil(((double) countAllShakedownTests / (double) Math.max(itemsPerPage,
				getItemsPerPage())));
	}

	public int getItemsPerPage() {
		return itemsPerPage < 1 ? 10 : itemsPerPage;
	}

	public boolean isShakedownTestViewSelected() {
		return selectedShakedownTestPresetView.equals(PresetViewOptions.SHAKEDOWN_TEST.getDisplayName());
	}

	public String getSortingIconForCol(String enumName) {
		String result = "icon icon-sortnone";
		shakedownTestScreenColDescriptor col = getEnumValueForString(enumName);

		if (col != null) {
			if (sortingDirection != null && sortingColumn != null && sortingColumn.equals(col)) {
				if (sortingDirection.equals(SortingDirectionType.ASC)) {
					result = "icon icon-sortasc";
				}
				else {
					result = "icon icon-sortdesc";
				}
			}
		}
		return result;
	}

	private shakedownTestScreenColDescriptor getEnumValueForString(String enumName) {
		shakedownTestScreenColDescriptor col = null;
		try {
			col = shakedownTestScreenColDescriptor.valueOf(enumName);
		}
		catch (IllegalArgumentException ie) {
			// no special treatment
		}
		catch (NullPointerException npe) {
			// no special treatment
		}
		return col;
	}

	public List<String> getShakedownTestPresetViewsOptions() {
		if (shakedowntestPresetViewsOptions == null) {
			shakedowntestPresetViewsOptions = new ArrayList<String>();
			initShakedownTestPresetViewsOptions();
		}

		return shakedowntestPresetViewsOptions;
	}

	private void initShakedownTestPresetViewsOptions() {
		for (PresetViewOptions viewOption : PresetViewOptions.values()) {
			shakedowntestPresetViewsOptions.add(viewOption.getDisplayName());
		}
	}

	public void setCurrentPage(int currentPage) {
		if (currentPage != 0 && currentPage != this.currentPage) {
			this.currentPage = currentPage;
			// loadApplicationServerGroups();
		}
	}

	public void initCreateTestPopupPanel() {
		renderCreateOrderDialog = true;
		if (asGroups == null) {
			loadApplicationServerGroups();
		}
		loadSuccessfulDeployments();
		selectedOrderAppServerGroupId = null;
		selectedOrderEnvironmentId = null;
	}

	private void loadSuccessfulDeployments() {
		contextIdMapWithSuccessfullLastDeployedAppServer = new HashMap<Integer, List<ResourceGroupEntity>>();
		List<Object[]> deployments = controller.getAllLastSucessfullDeployments();

		for (Object[] deployment : deployments) {
			Integer contextId = (Integer) deployment[0];
			ResourceGroupEntity group = (ResourceGroupEntity) deployment[1];
			if (contextIdMapWithSuccessfullLastDeployedAppServer.containsKey(contextId)) {
				contextIdMapWithSuccessfullLastDeployedAppServer.get(contextId).add(group);
			}
			else {
				List<ResourceGroupEntity> asList = new ArrayList<ResourceGroupEntity>();
				asList.add(group);
				contextIdMapWithSuccessfullLastDeployedAppServer.put(contextId, asList);
			}
		}
	}

	public void loadApplicationServerGroups() {
		asGroups = new HashMap<Integer, ResourceGroupEntity>();
		List<ResourceGroupEntity> asList = controller.loadAppServers();
		// filter group
		for (ResourceGroupEntity g : asList) {
			if (g != null) {
				if (!g.isAppServerContainer() && !asGroups.containsKey(g.getId())) {
					asGroups.put(g.getId(), g);
				}
			}
		}
	}



	private List<String> getAllAppDisplayNames() {
		if (allAppDisplayNames == null) {
			allAppDisplayNames = new ArrayList<>();
			for(ResourceGroupEntity group : controller.loadAllApplications()){
				allAppDisplayNames.add(group.getName());
			}
		}
		Collections.sort(allAppDisplayNames);
		return allAppDisplayNames;
	}

	public ShakedownTestEntity getSelectedShakedownTest() {
		if (selectedShakedownTestId != null) {
			for (ShakedownTestEntity sdTest : getShakedownTests()) {
				if (sdTest.getId().equals(selectedShakedownTestId)) {
					return sdTest;
				}
			}
		}

		return null;
	}

	public List<ShakedownTestEntity> getShakedownTests() {
		if (shakedownTests == null) {
			shakedownTests = new ArrayList<ShakedownTestEntity>();
		}
		return shakedownTests;
	}

	public List<String> getAllAppServerNames() {
		List<String> result = new ArrayList<String>();
		if (asGroups != null) {
			for(ResourceGroupEntity r : asGroups.values()){
				result.add(r.getName());
			}
			Collections.sort(result);
		}
		return result;
	}

	public String getSelectedShakedownTestPresetView() {
		return selectedShakedownTestPresetView;
	}

	public void setShakedownTests(List<ShakedownTestEntity> shakedownTests) {
		this.shakedownTests = shakedownTests;
	}

	public String getShakedownTestStateText(ShakedownTestEntity shakedownTest) {
		return shakedownTest.getShakedownTestState().getDisplayName();
	}

	public String getShakedownTestStateFrameCssClass(ShakedownTestEntity sdTest) {
		String result = EMPTY_STRING;

		switch (sdTest.getShakedownTestState()) {
		case success:
			result = "state success";
			break;
		case failed:
			result = "state failed";
			break;
		case warning:
			result = "state warning";
			break;
		case scheduled:
			result = "state scheduled";
			break;
		case inprogress:
			result = "state inprogress";
			break;
		}

		return result;
	}

	public List<String> getAllEnvNames() {
		List<String> result = new ArrayList<String>();
		for (ContextEntity ctx : contextDataProvider.getEnvironments()) {
			result.add(ctx.getName());
		}
		Collections.sort(result);
		return result;

	}

	public void setShakedownTestOrder(List<ShakedownTestOrder> shakedownTestOrder) {
		this.shakedownTestOrder = shakedownTestOrder;
	}

	public String getAllAppserverSelectedText() {
		return ShakedownTestOrder.allAppServerSelectedText;
	}

	private void initDefaultSelection() {
		sortingDirection = SortingDirectionType.DESC;
		sortingColumn = shakedownTestScreenColDescriptor.ID;
	}

	private List<String> getAllStates() {
		List<String> statesDisplayNames = new ArrayList<String>();
		for (shakedownTest_state state : shakedownTest_state.values()) {
			statesDisplayNames.add(state.getDisplayName());
		}

		Collections.sort(statesDisplayNames);

		return statesDisplayNames;
	}

	public String getLastSuccessfulDeploymentDate() {
		String result = "Not yet sucessfully deployed (by AMW)";
		ShakedownTestEntity test = getSelectedShakedownTest();
		if (test != null && test.getDeployment() != null
				&& test.getDeployment().getDeploymentDate() != null) {
			result = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(test.getDeployment()
					.getDeploymentDate());
		}
		return result;
	}

	private void loadAllReleases() {
		allReleases = releaseMgmtService.loadAllReleases(false);
		allReleasesMap = new HashMap<Integer, ReleaseEntity>();
		for (ReleaseEntity rel : allReleases) {
			allReleasesMap.put(rel.getId(), rel);
		}
	}

	/**
	 * loads a subSet of releases greater or equal to the first release in the selected group or an empty
	 * list if no group is selected
	 */
	public List<ReleaseEntity> getReleasesForAs() {
		List<ReleaseEntity> releasesForAs = null;
		setSelectedReleaseId(null);
		if (!showOnlyDeployedAppServers && selectedOrderAppServerGroupId != null
				&& asGroups.containsKey(selectedOrderAppServerGroupId)) {
			releasesForAs = releaseMgmtService.getDeployableReleasesForResourceGroup(asGroups
					.get(selectedOrderAppServerGroupId));
		}
		else if (!showOnlyDeployedAppServers && selectedOrderAppServerGroupId != null
				&& selectedOrderAppServerGroupId < 0) {
			// add all releases
			releasesForAs = new ArrayList<ReleaseEntity>(allReleases);
		}
		return releasesForAs;
	}

	public void setSelectedReleaseId(Integer selectedReleaseId) {
		if (selectedReleaseId != null && allReleasesMap.containsKey(selectedReleaseId)) {
			this.selectedRelease = allReleasesMap.get(selectedReleaseId);
		}
		else {
			this.selectedRelease = null;
		}
	}

	public Integer getSelectedReleaseId() {
		return selectedRelease != null ? selectedRelease.getId() : null;
	}

	public String formatDate(Date date) {
		Format formatter;
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return date != null ? formatter.format(date) : EMPTY_STRING;
	}

}