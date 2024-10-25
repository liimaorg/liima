import { ChangeDetectionStrategy, Component, computed, inject, Signal } from '@angular/core';
import { IconComponent } from '../../shared/icon/icon.component';
import { EnvironmentService } from '../../deployment/environment.service';
import { Environment, EnvironmentTree } from '../../deployment/environment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EnvironmentEditComponent } from './environment-edit/environment-edit.component';
import { takeUntil } from 'rxjs/operators';
import { BehaviorSubject, Subject } from 'rxjs';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';

@Component({
  selector: 'app-environments-page',
  standalone: true,
  imports: [IconComponent, LoadingIndicatorComponent],
  templateUrl: './environments-page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentsPageComponent {
  private environmentsService = inject(EnvironmentService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);
  //TODO handle permissions
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();
  contexts: Signal<Environment[]> = this.environmentsService.contexts;
  environmentTree: Signal<EnvironmentTree[]> = computed(() => {
    if (!this.contexts()) return;
    const envTree = this.buildEnvironmentTree(this.contexts());
    if (!(envTree.length > 0)) return;
    return envTree[0].children;
  });

  private buildEnvironmentTree(environments: Environment[], parentName: string | null = null): EnvironmentTree[] {
    return environments
      .filter((environment) => environment.parentName === parentName)
      .map((environment) => {
        return {
          id: environment.id,
          name: environment.name,
          nameAlias: environment.nameAlias,
          parentName: environment.parentName,
          parentId: environment.parentId,
          children: this.buildEnvironmentTree(environments, environment.name),
          selected: environment.selected,
          disabled: environment.disabled,
        };
      });
  }

  addEnvironment(envParent: EnvironmentTree) {
    const modalRef: NgbModalRef = this.modalService.open(EnvironmentEditComponent);
    modalRef.componentInstance.environment = {
      id: undefined,
      name: '',
      nameAlias: '',
      parent: envParent.name,
      parentId: envParent.id,
      selected: undefined,
      disabled: undefined,
    };
    modalRef.componentInstance.saveEnvironment
      .pipe(takeUntil(this.destroy$))
      .subscribe((environment: Environment) => this.save(environment));
  }

  save(environment: Environment) {
    this.environmentsService
      .save(environment)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Environment saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.environmentsService.refreshData(),
      });
  }
}
