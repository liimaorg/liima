import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { Action, AuthService } from '../../../auth/auth.service';
import { Resource } from '../../../resource/resource';
import { RelatedResourcesService } from '../../../resource/related-resources.service';
import { Subject } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TileListEntryOutput } from '../../../shared/tile/tile-list/tile-list.component';
import { RelatedResource } from '../../../resource/related-resource';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';

@Component({
  selector: 'app-related-resources-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './related-resources-list.component.html',
})
export class RelatedResourcesListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private relatedResourcesService = inject(RelatedResourcesService);
  private modalService = inject(NgbModal);
  private destroy$ = new Subject<void>();

  resource = input.required<Resource>();
  contextId = input.required<number>();
  relatedResources = this.relatedResourcesService.relatedResources;

  isLoading = computed(() => {
    if (this.resource() != null) {
      this.relatedResourcesService.setResourceId(this.resource().id);
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.resource()) {
      return {
        canShowRelatedResources: this.authService.hasPermission('RESOURCE', Action.READ),
        canAdd:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceGroupPermission('RESOURCE', Action.CREATE, this.resource().resourceGroupId),
        canDelete:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasResourceGroupPermission('RESOURCE', Action.DELETE, this.resource().resourceGroupId),
      };
    } else {
      return {
        canShowRelatedResources: false,
        canAdd: false,
        canDelete: false,
      };
    }
  });

  relatedResourcesData = computed(() => {
    if (this.relatedResources()?.length > 0) {
      const consumedResources = this.mapListEntries(
        this.relatedResources().filter((relatedResource) => relatedResource.relationType === 'consumed'),
      );
      const providedResources = this.mapListEntries(
        this.relatedResources().filter((relatedResource) => relatedResource.relationType === 'provided'),
      );

      const result = [];
      if (this.permissions().canShowRelatedResources) {
        result.push({
          title: 'Consumed Resources',
          entries: consumedResources,
          canDelete: this.permissions().canDelete,
        });
      }
      if (this.permissions().canShowRelatedResources) {
        result.push({
          title: 'Provided Resources',
          entries: providedResources,
          canDelete: this.permissions().canDelete,
        });
      }
      return result;
    } else return null;
  });

  doListAction($event: TileListEntryOutput) {
    this.deleteRelatedResource($event.id);
  }

  mapListEntries(relatedResources: RelatedResource[]) {
    return relatedResources.map((relatedResource) => ({
      name: relatedResource.relatedResourceName,
      description: relatedResource.relatedResourceRelease,
    }));
  }

  addRelatedResource() {
    this.modalService.open('This would open a modal to add a new related resource');
  }

  private deleteRelatedResource(id: number) {
    this.modalService.open('This would open a modal to remove resource relation with id: ' + id);
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }
}
