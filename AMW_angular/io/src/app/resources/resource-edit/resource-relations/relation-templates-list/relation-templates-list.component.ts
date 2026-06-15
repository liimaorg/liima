import { Component, computed, inject, input, OnDestroy } from '@angular/core';
import { ResourceRelation } from '../../../models/resource-relation';
import { LoadingIndicatorComponent } from '../../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../../shared/tile/tile.component';
import {
  EntryAction,
  TileListComponent,
  TileListEntryOutput
} from '../../../../shared/tile/tile-list/tile-list.component';
import { ResourceTemplate } from '../../../models/resource-template';
import { AuthService } from '../../../../auth/auth.service';
import { ResourceRelationsService } from '../../../services/resource-relations.service';
import { Resource } from '../../../models/resource';
import { Subject } from 'rxjs';

const RESOURCE_PERM = 'RESOURCE_TEMPLATE';
const RESOURCETYPE_PERM = 'RESOURCETYPE_TEMPLATE';

@Component({
  selector: 'app-relation-templates-list',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent, TileListComponent],
  templateUrl: './relation-templates-list.component.html',
})
export class RelationTemplatesListComponent implements OnDestroy {
  private authService = inject(AuthService);
  private resourceRelationsService = inject(ResourceRelationsService);
  private destroy$ = new Subject<void>();

  relation = input.required<ResourceRelation>();
  resource = input.required<Resource>();
  contextId = input.required<number>();
  templates = this.resourceRelationsService.relationTemplates;

  isLoading = computed(() => {
    if (this.relation() != null && this.resource() != null && this.contextId() != null) {
      this.resourceRelationsService.setIdsForRelationTemplates(this.resource().id, this.relation()?.id, this.contextId())
      return false;
    }
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.relation()) {
      return {
        canShowInstanceTemplates: this.authService.hasPermission(RESOURCE_PERM, 'READ'),
        canShowTypeTemplates: this.authService.hasPermission(RESOURCETYPE_PERM, 'READ'),
        canAdd:  (this.contextId() === 1 || this.contextId === null) &&
          (this.relation().relationType === 'consumed') &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'CREATE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
        canEdit:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'UPDATE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
        canDelete:
          (this.contextId() === 1 || this.contextId === null) &&
          this.authService.hasPermission(
            RESOURCE_PERM,
            'DELETE',
            this.resource().type,
            this.resource().resourceGroupId,
          ),
      };
    } else {
      return {
        canShowInstanceTemplates: false,
        canShowTypeTemplates: false,
        canAdd: false,
        canEdit: false,
        canDelete: false,
      };
    }
  });

  templatesData = computed(() => {
    if (this.templates()?.length > 0) {
      const instanceTemplates = this.mapListEntries(
        this.templates().filter((template) => template.sourceType === 'RESOURCE_RELATION'),
      );
      const typeTemplates = this.mapListEntries(
        this.templates().filter((template) => template.sourceType === 'RESOURCE_RELATION_TYPE'),
      );

      const result = [];
      if (instanceTemplates.length > 0 && this.permissions().canShowInstanceTemplates) {
        result.push({
          title: 'Instance Templates',
          entries: instanceTemplates,
          canEdit: this.permissions().canEdit,
          canDelete: this.permissions().canDelete,
        });
      }
      if (typeTemplates.length > 0 && this.permissions().canShowTypeTemplates) {
        result.push({
          title: 'Resource Type Relationship Templates',
          entries: typeTemplates,
          canEdit: false,
          canDelete: false,
        });
      }
      return result;
    } else return null;
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  mapListEntries(templates: ResourceTemplate[]) {
    return templates
      .sort((a, b) => a.name.localeCompare(b.name))
      .map((template) => ({
        name: template.name,
        description: template.targetPath,
        id: template.id,
      }));
  }

  addTemplate() {
    console.log('todo add template')
  }

  protected doListAction($event: TileListEntryOutput) {
    switch ($event.action) {
      case EntryAction.edit:
        this.editTemplate($event.id);
        return;
      case EntryAction.delete:
        this.deleteTemplate($event.id);
        return;
    }
  }

  private deleteTemplate(id: number) {
    console.log('todo delete template')

  }

  private editTemplate(id: number) {
    console.log('todo edit template')

  }
}
