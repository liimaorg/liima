import { Component, computed, inject, Signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { AuthService } from '../../auth/auth.service';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Resource } from '../../resource/resource';

@Component({
  selector: 'app-resources-edit',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent],
  templateUrl: './resource-edit.component.html',
})
export class ResourceEditComponent {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

  resource: Signal<Resource> = this.resourceService.resource;
  resourceId = toSignal(
    this.route.paramMap.pipe(
      map((params) => +params.get('id')),
      distinctUntilChanged(),
    ),
    -1,
  );

  isLoading = computed(() => {
    if (this.resourceId() > -1) {
      this.resourceService.getResource(this.resourceId());
      return false;
    }
  });
}
