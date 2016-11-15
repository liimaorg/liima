import {Component, OnInit} from "@angular/core";
import {Resource} from "./resource";
import {ResourceService} from "./resource.service";

@Component({
  selector: 'amw-resource-list',
  template: `
  <section>
    <section *ngIf="isLoading && !errorMessage">
    Retrieving data...
    </section>
    <section *ngIf="resource">
        <amw-resource [resource]="resource" (notifyType)='byType($event)' (notifyResourceGroup)='getResourceGroup($event)'></amw-resource>
    </section>
    <section *ngIf="resources">
      <ul>
        <li *ngFor="let resource of resources">
            <amw-resource [resource]="resource" (notifyType)='byType($event)' (notifyResourceGroup)='getResourceGroup($event)'></amw-resource>
        </li>
      </ul>
     </section>
     <section *ngIf="errorMessage">
        {{errorMessage}}
     </section>
  </section>
  `,
  providers: [ResourceService]
})
export class ResourceListComponent implements OnInit {
  resource: Resource = null;
  resources: Resource[] = [];
//  resourceGroupName: string = 'amw';
  errorMessage: string = '';
  isLoading: boolean = true;

  constructor(private resourceService: ResourceService) {
  }

  ngOnInit() {
    console.log('hello `ResourceList` component');
    this.resourceService
    //      .get(this.resourceGroupName)
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
