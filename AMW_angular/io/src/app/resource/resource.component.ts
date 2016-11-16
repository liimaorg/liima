import {Component, Input, Output, EventEmitter} from "@angular/core";
import {Resource} from "./resource";

@Component({
  selector: 'amw-resource',
  template: `
  <section *ngIf="resource.type != 'APPLICATION'">{{resource.name}}</section>
  <button *ngIf="resource.type === 'APPLICATION'" (click)="getResourceGroup(resource.name)">{{resource.name}}</button>
  <button (click)="byType(resource.type)">{{resource.type}}</button>
    <ul>
        <li *ngFor="let release of resource.releases">{{release.release}}</li>
    </ul>`
})

export class ResourceComponent {
  @Input() resource: Resource;
  @Output() notifyType: EventEmitter<string> = new EventEmitter<string>();
  @Output() notifyResourceGroup: EventEmitter<string> = new EventEmitter<string>();

  byType(type: string) {
    this.notifyType.emit(type);
  }

  getResourceGroup(resourceGroupName: string) {
    this.notifyResourceGroup.emit(resourceGroupName);
  }


}


