import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Resource } from './resource';

@Component({
  selector: 'amw-resource',
  template: `

    Resource:
    <br><a routerLink="/resource/{{resource.name}}">{{resource.name}}</a>
    <br>Type:<br>
    <button (click)="byType(resource.type)">{{resource.type}}</button>
    <br><a routerLink="/resource/type/{{resource.type}}">{{resource.type}}</a>
    <ul>
        <li *ngFor="let release of resource.releases">
            <a routerLink="/resource/{{resource.name}}/{{release.release}}">{{release.release}}</a>
        </li>
    </ul>`
})

export class ResourceComponent {

  @Input() resource: Resource;
  @Output() notifyType: EventEmitter<string> = new EventEmitter<string>();

  byType(type: string) {
    this.notifyType.emit(type);
  }
}
