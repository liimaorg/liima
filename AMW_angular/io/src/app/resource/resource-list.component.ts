import { Component, OnInit } from '@angular/core';
import { Resource } from './resource';
import { ResourceService } from './resource.service';

@Component({
  selector: 'amw-resource-list',
  templateUrl: './resource-list.component.html',
  providers: [ResourceService]
})
export class ResourceListComponent implements OnInit {
  resource: Resource = null;
  resources: Resource[] = [];
  resourceInRelease: Resource = null;
  titleLabel: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private resourceService: ResourceService) {
  }

  ngOnInit() {
    console.log('hello `ResourceList` component');
  }

  getAllResources() {
    this.isLoading = true;
    this.resource = null;
    this.resourceInRelease = null;
    this.titleLabel = 'Alle';
    this.resourceService
      .getAll()
      .subscribe(
        /* happy path */ r => this.resources = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  byType(type: string) {
    this.isLoading = true;
    this.resource = null;
    this.resourceInRelease = null;
    this.titleLabel = 'Alle vom Typ '+type;
    this.resourceService
      .getByType(type)
      .subscribe(
        /* happy path */ r => this.resources = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  getResourceGroup(resourceGroupName: string) {
    this.isLoading = true;
    this.resources = [];
    this.resourceInRelease = null;
    this.titleLabel = 'Gruppe '+resourceGroupName;
    this.resourceService
      .get(resourceGroupName)
      .subscribe(
        /* happy path */ r => this.resource = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  getInRelease(prop: Object) {
    this.isLoading = true;
    this.resource = null;
    this.titleLabel = 'Gruppe ' +prop['resourceGroupName']+ ' in Release ' +prop['releaseName'];
    this.resourceService
      .getInRelease(prop['resourceGroupName'], prop['releaseName'])
      .subscribe(
        /* happy path */ r => this.resourceInRelease = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }
}
