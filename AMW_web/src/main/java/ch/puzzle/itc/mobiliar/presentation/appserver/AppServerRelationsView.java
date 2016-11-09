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

package ch.puzzle.itc.mobiliar.presentation.appserver;

import java.io.Serializable;
import java.util.*;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import ch.puzzle.itc.mobiliar.business.appserverrelation.boundary.AppServerRelation;
import ch.puzzle.itc.mobiliar.business.appserverrelation.control.AppServerRelationPath;
import ch.puzzle.itc.mobiliar.business.releasing.control.ReleaseMgmtService;
import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceEntity;
import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.ResourceGroupEntity;
import ch.puzzle.itc.mobiliar.presentation.ViewBackingBean;
import ch.puzzle.itc.mobiliar.presentation.util.GlobalMessageAppender;

@ViewBackingBean
public class AppServerRelationsView implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	AppServerRelation service;

	@Inject
	ReleaseMgmtService releaseService;

	@Getter
	Integer appServerId;

	@Getter
	List<AppServerRelationPath> paths;

	@Getter
	List<ResourceGroupEntity> resourceCandidates;

	@Getter
	AppServerRelationPath currentEditPath;

	@Getter
	@Setter
	Integer releaseId;

	@Getter
	ResourceEntity appServer;

	@Getter
	List<ReleaseEntity> releases;

    @Getter
    List<AppServerRelationSoftlinkModel> softlinkModels;

	private Map<Integer,Map<ResourceEntity,ResourceEntity>> providingResources;

    private List<Integer> cpisHavingSoftlinks;


	/**
	 * Set appServerId and loads the data from database for given id.
	 */
	public void setAppServerId(Integer appServerId) {
		if (appServerId == null) {
			resetServerParameter();
		}
		else if (isOtherAppserverId(appServerId)) {
			loadDataAndSetServerParameter(appServerId);
		}
		this.appServerId = appServerId;
	}

    private void loadSoftLinkResources() {
        this.softlinkModels = new LinkedList<>();
        this.cpisHavingSoftlinks = new ArrayList<>();
        List<Integer> resourceIds = new ArrayList<>();
        for (AppServerRelationPath path : paths) {
            resourceIds.add(path.getMasterId());
        }
        if (!resourceIds.isEmpty()) {
            this.providingResources = service.getAllSoftlinkRelatedResources(resourceIds);
            populateSoftlinkModel();
        }
    }

    private void populateSoftlinkModel() {
        for (AppServerRelationPath path : paths) {
            if (!cpisHavingSoftlinks.contains(path.getMasterId()) && providingResources.containsKey(path.getMasterId())){
				Set<Map.Entry<ResourceEntity, ResourceEntity>> entries = providingResources.get(path.getMasterId()).entrySet();
				for (Map.Entry<ResourceEntity, ResourceEntity> entry : entries) {
					StringBuilder sb = new StringBuilder(path.getDisplayablePath().substring(0,path.getDisplayablePath().lastIndexOf(":")+1));
					sb.append(entry.getKey().getName());
					final AppServerRelationSoftlinkModel aModel = new AppServerRelationSoftlinkModel(entry.getKey(),entry.getValue(),sb.toString());
					this.softlinkModels.add(aModel);
				}
                cpisHavingSoftlinks.add(path.getMasterId());
            }
        }
    }

	private void loadDataAndSetServerParameter(Integer appServerId) {
		this.appServer = service.getAppServer(appServerId);
		releases = releaseService.getDeployableReleasesForResourceGroup(appServer.getResourceGroup());
		ReleaseEntity currentRelease = getCurrentRelease();

		paths = service.getAppServerRelationsFromLiveDB(appServerId, currentRelease);
		Collections.sort(paths, sortingLogic);
        loadSoftLinkResources();
	}

	private ReleaseEntity getCurrentRelease() {
		ReleaseEntity currentRelease = null;
		if (releaseId == null || releaseId.equals(0)) {
            releaseId = appServer.getRelease().getId();
        }
		for (ReleaseEntity r : releases) {
            if (r.getId().equals(releaseId)) {
                currentRelease = r;
                break;
            }
        }
		return currentRelease;
	}

	private boolean isOtherAppserverId(Integer appServerId) {
		return !appServerId.equals(this.appServerId);
	}

	private void resetServerParameter() {
		paths = Collections.emptyList();
		appServer = null;
		releases = Collections.emptyList();
	}


	public void setCurrentEditPath(AppServerRelationPath currentEditPath) {
        this.currentEditPath = currentEditPath;
		this.resourceCandidates = service.getPotentialResources(currentEditPath);
	}

	public boolean isCurrentlySelected(ResourceGroupEntity res) {
		if (res != null && this.currentEditPath != null) {
			if (this.currentEditPath.getSelectedResourceGroup() != null
					&& this.currentEditPath.getSelectedResourceGroup().getId().equals(res.getId())) {
				return true;
			}
			else if (this.currentEditPath.getSelectedResourceGroup() == null
					&& this.currentEditPath.getAppserverRelation() != null
					&& this.currentEditPath.getAppserverRelation().getOverriddenSlaveResource() != null
					&& this.currentEditPath.getAppserverRelation().getOverriddenSlaveResource().getId()
							.equals(res.getId())) {
				return true;
			}
		}
		return false;
	}

	public boolean isRedefined(AppServerRelationPath path) {
		return path.isRedefined() || path.getSelectedResourceGroup() != null;
	}

	public void reset(AppServerRelationPath pathToReset) {
		if (pathToReset != null) {
			pathToReset.setSelectedResourceGroup(null);
			if (pathToReset.getAppserverRelation() != null
					&& pathToReset.getAppserverRelation().getOverriddenSlaveResource() != null) {
				pathToReset.getAppserverRelation().setOverriddenSlaveResource(null);
			}
		}
	}

	public void defineAppServerRelation(ResourceGroupEntity resGroup) {
		if (currentEditPath != null && currentEditPath.getLastRelationOfPath() != null) {
			// Update an existing appserver relation
			currentEditPath.setSelectedResourceGroup(resGroup);

		}
	}

	public void save() {
		service.storeAppServerRelationPaths(paths);
		GlobalMessageAppender.addSuccessMessage("Successfully saved all app server relations");
	}

	private final static Comparator<AppServerRelationPath> sortingLogic = new Comparator<AppServerRelationPath>() {

		private int nullSafeStringComparison(String s1, String s2) {
			if (s1 == null && s2 == null) {
				return 0;
			}
			else if (s1 == null && s2 != null) {
				return 1;
			}
			else if (s1 != null && s2 == null) {
				return -1;
			}
			else {
				return s1.compareTo(s2);
			}
		}

		@Override
		public int compare(AppServerRelationPath o1, AppServerRelationPath o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				}
				else {
					return 1;
				}
			}
			else if (o2 == null) {
				return -1;
			}
			else {
				Integer o1PathLength = o1.getPath() == null ? 0 : o1.getPath().size();
				Integer o2PathLength = o2.getPath() == null ? 0 : o2.getPath().size();
				int pathLengthCompared = o1PathLength.compareTo(o2PathLength);
				if (pathLengthCompared == 0) {
					return nullSafeStringComparison(o1.getDisplayablePath(), o2.getDisplayablePath());
				}
				return pathLengthCompared;
			}

		}

	};

}
