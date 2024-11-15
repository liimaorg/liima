import { Component, input } from '@angular/core';
import { Resource } from '../../resource/resource';
import { ResourceType } from '../../resource/resource-type';

@Component({
  selector: 'app-resources-list',
  standalone: true,
  templateUrl: './resources-list.component.html',
  imports: [],
})
export class ResourcesListComponent {
  resourceType = input.required<ResourceType>();
  resourceGroupList = input<Resource[]>();
}
