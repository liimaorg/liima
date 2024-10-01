import { Component, OnInit, inject } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { Function } from './function';
import { FunctionsService } from './functions.service';
import { FunctionEditComponent } from './function-edit.component';
import { FunctionDeleteComponent } from './function-delete.component';

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
  ],
  templateUrl: './functions.component.html',
})
export class FunctionsComponent implements OnInit {
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private functionsService = inject(FunctionsService);
  private toastService = inject(ToastService);

  functions$: Observable<Function[]>;

  private error$ = new BehaviorSubject<string>('');
  private destroy$ = new Subject<void>();

  canManage: boolean = false;
  canView: boolean = false;

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
    this.getUserPermissions();
    this.getFunctions();
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    this.canManage = this.authService.hasPermission('MANAGE_GLOBAL_FUNCTIONS', 'ALL');
    this.canView = this.authService.hasPermission('VIEW_GLOBAL_FUNCTIONS', 'ALL');
  }

  private getFunctions() {
    this.functions$ = this.functionsService.getAllFunctions();
  }

  addFunction() {
    const modalRef = this.modalService.open(FunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = {
      id: 0,
      name: '',
      content: '',
    };
    modalRef.componentInstance.canManage = this.canManage;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: Function) => this.save(functionData));
  }

  editFunction(functionData: Function) {
    const modalRef = this.modalService.open(FunctionEditComponent, {
      size: 'xl',
    });
    modalRef.componentInstance.function = functionData;
    modalRef.componentInstance.canManage = this.canManage;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: Function) => this.save(functionData));
    this.functions$ = this.functionsService.getAllFunctions();
  }

  save(functionData: Function) {
    this.functionsService
      .addFunction(functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.getFunctions(),
      });
  }

  deleteFunction(functionData: Function) {
    const modalRef = this.modalService.open(FunctionDeleteComponent);
    modalRef.componentInstance.function = functionData;
    modalRef.componentInstance.deleteFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: Function) => this.delete(functionData));
  }

  delete(functionData: Function) {
    this.functionsService
      .deleteFunction(functionData.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function deleted.'),
        error: (e) => this.error$.next(e),
        complete: () => this.getFunctions(),
      });
  }
}
