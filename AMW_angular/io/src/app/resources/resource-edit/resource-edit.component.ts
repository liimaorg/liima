import { Component, computed, inject, signal } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { AuthService } from '../../auth/auth.service';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { toSignal } from '@angular/core/rxjs-interop';
import { ResourceService } from '../../resource/resource.service';

@Component({
  selector: 'app-resources-edit',
  standalone: true,
  imports: [LoadingIndicatorComponent, PageComponent],
  templateUrl: './resource-edit.component.html',
})
export class ResourceEditComponent {
  private authService = inject(AuthService);
  private resourceService = inject(ResourceService);
  private route = inject(ActivatedRoute);

  isLoading = signal(false);

  resourceId = toSignal(
    this.route.paramMap.pipe(
      map((params) => +params.get('id')),
      distinctUntilChanged(),
    ),
    -1,
  );

  resource = computed(() => {
    if (this.resourceId() > -1) {
      return { id: this.resourceId(), name: 'MyResource' };
    }
  });
}
