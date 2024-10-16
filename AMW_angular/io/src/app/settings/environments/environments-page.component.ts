import { Component, inject, OnInit } from '@angular/core';
import { IconComponent } from '../../shared/icon/icon.component';
import { EnvironmentService } from '../../deployment/environment.service';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { BehaviorSubject, Subject } from 'rxjs';
import { Environment, EnvironmentTree } from '../../deployment/environment';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-environments-page',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './environments-page.component.html',
})
export class EnvironmentsPageComponent implements OnInit {
  private environmentsService = inject(EnvironmentService);
  private toastService = inject(ToastService);
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();
  environmentTree: EnvironmentTree[];

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
    //this.getUserPermissions(); //TODO Handle Permissons
    this.environmentsService.getContexts().subscribe((contexts) => {
      this.environmentTree = this.buildEnvironmentTree(contexts)[0].children;
    });
  }

  private buildEnvironmentTree(environments: Environment[], parentName: string | null = null): EnvironmentTree[] {
    const envTree: EnvironmentTree[] = environments
      .filter((environment) => environment.parent === parentName) // Find items with the current parentId
      .map(
        (environment) =>
          new EnvironmentTree(
            environment.id,
            environment.name,
            environment.nameAlias,
            this.buildEnvironmentTree(environments, environment.name),
            environment.selected,
            environment.disabled,
          ),
      );
    return envTree;
  }
}
