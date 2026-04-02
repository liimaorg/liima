import { Component, input } from '@angular/core';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';

@Component({
  selector: 'app-resource-relations',
  standalone: true,
  imports: [TileComponent, LoadingIndicatorComponent],
  templateUrl: './resource-relations.component.html',
  styleUrl: './resource-relations.component.scss',
})
export class ResourceRelationsComponent {
  contextId = input.required<number>();

  isLoading = false;
}
