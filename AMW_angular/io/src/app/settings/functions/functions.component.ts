import { Component, inject, Signal, computed, OnDestroy } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Subject } from 'rxjs';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { AppFunction } from './appFunction';
import { FunctionsService } from './functions.service';
import { FunctionEditComponent } from './function-edit.component';
import { FunctionDeleteComponent } from './function-delete.component';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-functions',
  standalone: true,
  imports: [
    AsyncPipe,
    IconComponent,
    LoadingIndicatorComponent,
    PaginationComponent,
    FunctionEditComponent,
    FunctionDeleteComponent,
    ButtonComponent,
  ],
  templateUrl: './functions.component.html',
})
export class FunctionsComponent implements OnDestroy {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(FunctionsService);
  private toastService = inject(ToastService);

  functions: Signal<AppFunction[]> = this.functionsService.functions;

  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  canManage: boolean = false;
  canView: boolean = false;

  loadingPermissions = computed(() => {
    if (this.authService.restrictions().length > 0) {
      this.getUserPermissions();
    } else {
      return `<div>Could not load permissions</div>`;
    }
  });

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    this.canManage = this.authService.hasPermission('MANAGE_GLOBAL_FUNCTIONS', 'ALL');
    this.canView = this.authService.hasPermission('VIEW_GLOBAL_FUNCTIONS', 'ALL');
  }

  addFunction() {
    const modalRef = this.modalService.open(FunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = {
      id: null,
      name: '',
      content: '',
    };
    modalRef.componentInstance.canManage = this.canManage;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: AppFunction) => this.saveNew(functionData));
  }

  editFunction(functionData: AppFunction) {
    const modalRef = this.modalService.open(FunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = functionData;
    modalRef.componentInstance.canManage = this.canManage;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: AppFunction) => this.saveModified(functionData));
  }

  saveNew(functionData: AppFunction) {
    this.functionsService
      .addNewFunction(functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.functionsService.refreshData(),
      });
  }

  saveModified(functionData: AppFunction) {
    this.functionsService
      .modifyFunction(functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.functionsService.refreshData(),
      });
  }

  deleteFunction(functionData: AppFunction) {
    const modalRef = this.modalService.open(FunctionDeleteComponent);
    modalRef.componentInstance.function = functionData;
    modalRef.componentInstance.deleteFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: AppFunction) => this.delete(functionData));
  }

  delete(functionData: AppFunction) {
    this.functionsService
      .deleteFunction(functionData.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function deleted.'),
        error: (e) => this.error$.next(e),
        complete: () => this.functionsService.refreshData(),
      });
  }
}
