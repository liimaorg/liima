import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { PageComponent } from '../layout/page/page.component';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-servers-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageComponent, LoadingIndicatorComponent],
  template: ` <app-loading-indicator [isLoading]="isLoading()"></app-loading-indicator>
    <app-page>
      <div class="page-title">Servers</div>
      <div class="page-content">
        {{ permissions() }}
      </div></app-page
    >`,
})
export class ServersPageComponent {
  private authService = inject(AuthService);

  isLoading = signal(false);

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canViewSomething: true,
      };
    } else {
      return { canViewSomething: false };
    }
  });
}
