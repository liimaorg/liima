import { ChangeDetectionStrategy, Component, computed, inject, Signal } from '@angular/core';
import { IconComponent } from '../../shared/icon/icon.component';
import { EnvironmentService } from '../../deployment/environment.service';
import { Environment, EnvironmentTree, EnvironmentTreeUtils } from '../../deployment/environment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EnvironmentEditComponent } from './environment-edit/environment-edit.component';
import { takeUntil } from 'rxjs/operators';
import { BehaviorSubject, Subject } from 'rxjs';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { EnvironmentDeleteComponent } from './environment-delete/environment-delete.component';
import { AuthService } from '../../auth/auth.service';
import { ButtonComponent } from '../../shared/button/button.component';
import { TableComponent, TableColumnType } from '../../shared/table/table.component';

@Component({
  selector: 'app-environments-page',
  standalone: true,
  imports: [IconComponent, ButtonComponent, TableComponent],
  templateUrl: './environments-page.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentsPageComponent {
  private authService = inject(AuthService);
  private environmentsService = inject(EnvironmentService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);
  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();
  contexts: Signal<Environment[]> = this.environmentsService.contexts;
  globalEnv: EnvironmentTree;
  environmentTree: Signal<EnvironmentTree[]> = computed(() => {
    if (!this.contexts()) return;
    const envTree = this.buildEnvironmentTree(this.contexts());
    this.globalEnv = envTree[0];
    if (!(envTree.length > 0)) return;
    return envTree[0].children;
  });

  private getUserPermissions() {
    canAdd: this.authService.hasPermission('ADD_NEW_ENV_OR_DOM', 'ALL');
    canDelete: this.authService.hasPermission('REMOVE_ENV_OR_DOM', 'ALL');
    canEdit: this.authService.hasPermission('EDIT_ENV_OR_DOM_NAME', 'ALL');
  }

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      return {
        canAdd: this.authService.hasPermission('ADD_NEW_ENV_OR_DOM', 'ALL'),
        canDelete: this.authService.hasPermission('REMOVE_ENV_OR_DOM', 'ALL'),
        canEdit: this.authService.hasPermission('EDIT_ENV_OR_DOM_NAME', 'ALL'),
      };
    } else {
      return {
        canAdd: false,
        canDelete: false,
        canEdit: false,
      };
    }
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
    modalRef.componentInstance.globalName = this.globalEnv.name;
    modalRef.componentInstance.environment = {
      id: undefined,
      name: '',
      nameAlias: '',
      parentName: envParent.name,
      parentId: envParent.id,
      selected: undefined,
      disabled: undefined,
    };
    modalRef.componentInstance.saveEnvironment
      .pipe(takeUntil(this.destroy$))
      .subscribe((environment: Environment) => this.save(environment));
  }

  addDomain() {
    const modalRef: NgbModalRef = this.modalService.open(EnvironmentEditComponent);
    modalRef.componentInstance.globalName = this.globalEnv.name;
    modalRef.componentInstance.environment = {
      id: undefined,
      name: '',
      nameAlias: '',
      parentName: this.globalEnv.name,
      parentId: this.globalEnv.id,
      selected: undefined,
      disabled: undefined,
    };
    modalRef.componentInstance.saveEnvironment
      .pipe(takeUntil(this.destroy$))
      .subscribe((environment: Environment) => this.save(environment));
  }

  editContext(environmentTreeId: number) {
    const modalRef: NgbModalRef = this.modalService.open(EnvironmentEditComponent);
    modalRef.componentInstance.globalName = this.globalEnv.name;
    modalRef.componentInstance.environment = EnvironmentTreeUtils.searchById(this.environmentTree(), environmentTreeId);
    modalRef.componentInstance.saveEnvironment
      .pipe(takeUntil(this.destroy$))
      .subscribe((environment: Environment) => this.save(environment));
  }

  deleteContext(environmentTreeId: number) {
    const modalRef: NgbModalRef = this.modalService.open(EnvironmentDeleteComponent);
    modalRef.componentInstance.globalName = this.globalEnv.name;
    modalRef.componentInstance.environment = EnvironmentTreeUtils.searchById(this.environmentTree(), environmentTreeId);
    modalRef.componentInstance.deleteEnvironment
      .pipe(takeUntil(this.destroy$))
      .subscribe((environment: Environment) => this.delete(environment.id));
  }

  save(environment: Environment) {
    this.environmentsService
      .save(environment)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Context saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.environmentsService.refreshData(),
      });
  }

  delete(id: number) {
    this.environmentsService
      .delete(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastService.success('Context deleted successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.environmentsService.refreshData(),
      });
  }

  environmentHeader(): TableColumnType<Environment>[] {
    return [
      {
        key: 'name',
        columnTitle: 'Environment name',
      },
      {
        key: 'nameAlias',
        columnTitle: 'Environment alias',
      },
    ];
  }
}
