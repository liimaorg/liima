import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ResourceDependency } from '../../models/resource-dependency';

@Component({
  selector: 'app-dependencies-table',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dependencies-table.component.html',
  styleUrl: './dependencies-table.component.scss',
})
export class DependenciesTableComponent {
  @Input() title: string = '';
  @Input() dependencies: ResourceDependency[] = [];
  @Input() additionalClasses: string = '';
}
