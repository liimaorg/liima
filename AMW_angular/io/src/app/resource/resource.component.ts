import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Resource } from './resource';

@Component({
  selector: 'amw-resource',
  template: `
    Resource:<br>
    <button (click)="getResourceGroup(resource.name)">{{resource.name}}</button>
    <br>Type:<br>
    <button (click)="byType(resource.type)">{{resource.type}}</button>
    <ul>
        <button *ngFor="let release of resource.releases" (click)="forRelease(resource.name, release.release)">
            {{release.release}}
        </button>
    </ul>`
})

export class ResourceComponent {
  @Input() resource: Resource;
  @Output() notifyType: EventEmitter<string> = new EventEmitter<string>();
  @Output() notifyResourceGroup: EventEmitter<string> = new EventEmitter<string>();
  @Output() notifyRelease: EventEmitter<Object> = new EventEmitter<Object>();

  byType(type: string) {
    this.notifyType.emit(type);
  }

  getResourceGroup(resourceGroupName: string) {
    this.notifyResourceGroup.emit(resourceGroupName);
  }

  forRelease(resourceGroupName: string, releaseName: string) {
    var props: Object = { resourceGroupName: resourceGroupName, releaseName: releaseName };
    this.notifyRelease.emit(props);
  }

}


