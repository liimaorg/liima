import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Restriction } from './restriction';

@Component({
  selector: 'amw-restriction',
  template: `
    <div style="margin-top:8px">
    Permission: {{restriction.permission}}
    <br>
    Action: {{restriction.action}}
    <br>
    Environment: {{restriction.contextName}}
    <br>
    ResourceType: {{restriction.resourceTypeName}}
    <br>
    ResourceTypePermission: {{restriction.resourceTypePermission}}
    <br>
    <button (click)="removeRestriction(restriction.id)">delete</button>
    </div>
`
})

export class RestrictionComponent {

  @Input() restriction: Restriction;
  @Output() deleteRestriction: EventEmitter<number> = new EventEmitter<number>();

  removeRestriction(id: number) {
    this.deleteRestriction.emit(id);
  }
}
