import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { Resource } from '../../resource/resource';

@Component({
  selector: 'app-resources-edit',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent],
  templateUrl: './resource-edit-page.component.html',
})
export class ResourceEditPageComponent {
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

  addFunction() {
    this.modalService.open('This would open a modal to add a function');
  }
}
