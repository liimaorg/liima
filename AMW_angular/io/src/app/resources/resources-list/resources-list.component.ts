import { Component, input } from '@angular/core';
import { Resource } from '../../resource/resource';
import { ResourceType } from '../../resource/resource-type';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';

@Component({
  selector: 'app-resources-list',
  standalone: true,
  templateUrl: './resources-list.component.html',
  imports: [ButtonComponent, IconComponent],
})
export class ResourcesListComponent {
  resourceType = input.required<ResourceType>();
  resourceGroupList = input<Resource[]>();
}
