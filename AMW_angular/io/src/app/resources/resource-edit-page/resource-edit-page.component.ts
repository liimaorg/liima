import { Component, computed, inject, Signal, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { Resource } from '../../resource/resource';
import { TileListEntry } from '../../shared/tile/tile-list/tile-list.component';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-resources-edit',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent],
  templateUrl: './resource-edit-page.component.html',
})
export class ResourceEditPageComponent {
  private modalService = inject(NgbModal);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

  resource: Signal<Resource> = this.resourceService.resource;
  ids = toSignal(
    this.route.queryParamMap.pipe(
      map((params) => params),
      distinctUntilChanged(),
    ),
    [],
  );

  isLoading = computed(() => {
    if (this.ids()?.keys) {
      // TODO show correct STAGE by context id
      this.resourceService.getResource(Number(this.ids().get('id')));
      return false;
    }
  });

  templatesData = signal([
    {
      title: 'Instance Templates',
      entries: [
        { name: 'startJob_0.sh', description: 'startJob_0.sh' },
        { name: 'startJob_1.sh', description: 'job 2 again' },
      ] as TileListEntry[],
      canEdit: true,
      canDelete: true,
    },
    {
      title: 'Resource Type Templates',
      entries: [{ name: 'seg', description: 'segmentation' }] as TileListEntry[],
      canOverwrite: false,
    },
  ]);

  functionsData = signal([
    {
      title: 'Resource Instance Functions',
      entries: [
        { name: 'Function1', description: 'bla' },
        { name: 'Function 2', description: 'whatever' },
      ],
      canEdit: true,
      canDelete: true,
    },
    {
      title: 'Resource Type Functions',
      entries: [{ name: 'seg', description: 'segmentation' }] as TileListEntry[],
      canOverwrite: false,
    },
  ]);

  add() {
    this.modalService.open('This would open a modal to add something');
  }

  doListAction($event: string) {
    console.log('whatever ' + $event);
  }
}
