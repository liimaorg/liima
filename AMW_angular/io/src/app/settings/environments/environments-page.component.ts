import { ChangeDetectionStrategy, Component, computed, inject, Signal } from '@angular/core';
import { IconComponent } from '../../shared/icon/icon.component';
import { EnvironmentService } from '../../deployment/environment.service';
import { Environment, EnvironmentTree } from '../../deployment/environment';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-environments-page',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './environments-page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentsPageComponent {
  private environmentsService = inject(EnvironmentService);
  //TODO Handle permissions
  contexts: Signal<Environment[]> = toSignal(this.environmentsService.getContexts());
  environmentTree: Signal<EnvironmentTree[]> = computed(() => {
    if (!this.contexts()) return;
    return this.buildEnvironmentTree(this.contexts())[0].children;
  });

  private buildEnvironmentTree(environments: Environment[], parentName: string | null = null): EnvironmentTree[] {
    return environments
      .filter((environment) => environment.parent === parentName)
      .map((environment) => {
        return {
          id: environment.id,
          name: environment.name,
          nameAlias: environment.nameAlias,
          children: this.buildEnvironmentTree(environments, environment.name),
          selected: environment.selected,
          disabled: environment.disabled,
        };
      });
  }
}
