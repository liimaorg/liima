import {Component, OnInit} from "@angular/core";
import {Resource} from "./resource";
import {ResourceService} from "./resource.service";

@Component({
  selector: 'amw-resource-list',
  templateUrl: './resource-list.component.html',
  providers: [ResourceService]
})
export class ResourceListComponent implements OnInit {
  resource: Resource = null;
  resources: Resource[] = [];
  errorMessage: string = '';
  isLoading: boolean = true;

  constructor(private resourceService: ResourceService) {
  }

  ngOnInit() {
    console.log('hello `ResourceList` component');
    this.resourceService
      .getAll()
      .subscribe(
        /* happy path */ r => this.resources = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  byType(type: string) {
    this.resourceService
      .getByType(type)
      .subscribe(
        /* happy path */ r => this.resources = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  getResourceGroup(resourceGroupName: string) {
    this.resourceService
      .get(resourceGroupName)
      .subscribe(
        /* happy path */ r => this.resource = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }
}
