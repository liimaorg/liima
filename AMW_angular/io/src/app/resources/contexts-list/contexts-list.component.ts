import { Component, computed, inject, Signal } from '@angular/core';
import { AuthService } from '../../auth/auth.service';
import { EnvironmentService } from '../../deployment/environment.service';
import { EnvironmentTree } from '../../deployment/environment';
import { NgClass, UpperCasePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-contexts-list',
  imports: [NgClass, UpperCasePipe],
  templateUrl: './contexts-list.component.html',
  styleUrl: './contexts-list.component.scss',
})
export class ContextsListComponent {
  private authService = inject(AuthService);
  private environmentsService = inject(EnvironmentService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  environmentTree: Signal<EnvironmentTree[]> = this.environmentsService.environmentTree;
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });

  selection = computed(() => {
    const ctxId = this.contextId();
    return this.environmentsService.findEnvironmentById(this.environmentTree(), ctxId) || this.environmentTree()[0];
  });

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canView: this.authService.hasPermission('ENV_PANEL_LIST', 'ALL'),
      };
    }
    return {
      canView: false,
    };
  });

  protected setContext(domain: EnvironmentTree) {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { ctx: domain.id },
      queryParamsHandling: 'merge',
    });
  }
}
