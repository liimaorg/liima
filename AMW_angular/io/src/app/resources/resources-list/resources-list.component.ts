import { Component, input } from '@angular/core';
import { Resource } from '../../resource/resource';

@Component({
  selector: 'app-resources-list',
  standalone: true,
  templateUrl: './resources-list.component.html',
  imports: [],
})
export class ResourcesListComponent {
  resourceGroupList = input<Resource[]>();
}
