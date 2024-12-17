import { Component, computed, inject, input } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceFunctionsService } from './resource-functions.service';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { TileComponent } from '../../../shared/tile/tile.component';
import { ResourceFunction } from './resource-function';

function mapListEntries(functions: ResourceFunction[]) {
  return functions.map((element) => ({ name: element.name, description: element.miks.join(', ') }));
}

function splitFunctions(resourceFunctions: ResourceFunction[]) {
  const [instance, resource] = [[], []];
  resourceFunctions.forEach((element) => (element.definedOnResourceType ? resource : instance).push(element));
  return [mapListEntries(instance), mapListEntries(resource)];
}

@Component({
  selector: 'app-resources-edit-functions',
  standalone: true,
  imports: [LoadingIndicatorComponent, TileComponent],
  templateUrl: './resource-edit-functions.component.html',
})
export class ResourceEditFunctionsComponent {
  private modalService = inject(NgbModal);
  private functionsService = inject(ResourceFunctionsService);

  resourceId = input.required<number>();
  functions = this.functionsService.functions;

  isLoading = computed(() => {
    if (this.resourceId() != null) {
      this.functionsService.getResourceFunctions(this.resourceId());
      return false;
    }
  });

  functionsData = computed(() => {
    if (this.functions().length > 0) {
      const [instance, resource] = splitFunctions(this.functions());
      return [
        {
          title: 'Resource Instance Functions',
          entries: instance,
          canEdit: true,
          canDelete: true,
        },
        {
          title: 'Resource Type Functions',
          entries: resource,
          canOverwrite: false,
        },
      ];
    }
  });

  add() {
    this.modalService.open('This would open a modal to add something');
  }

  doListAction($event: string) {
    console.log('whatever ' + $event);
  }
}
