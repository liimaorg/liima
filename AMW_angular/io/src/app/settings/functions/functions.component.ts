import { Component, OnInit, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService, isAllowed } from '../../auth/auth.service';
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
  count$: Observable<number>;
  private error$ = new BehaviorSubject<string>('');

  private destroy$ = new Subject<void>();

  maxResults = 10;
  offset = 0;
  currentPage: number;
  lastPage: number;

  isLoading = true;

  canCreate = signal<boolean>(false);
  canEdit = signal<boolean>(false);
  canDelete = signal<boolean>(false);

  ngOnInit(): void {
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      msg !== '' ? this.toastService.error(msg) : null;
    });
    this.getUserPermissions();
    this.count$ = this.functionsService.getCount();
    this.getFunctions();
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    const actions = this.authService.getActionsForPermission('FUNCTION');
    this.canCreate.set(actions.some(isAllowed('CREATE')));
    this.canEdit.set(actions.some(isAllowed('UPDATE')));
    this.canDelete.set(actions.some(isAllowed('DELETE')));
  }

  private getFunctions() {
    this.isLoading = true;
    this.functions$ = this.functionsService.getFunctions(this.offset, this.maxResults);
    this.currentPage = Math.floor(this.offset / this.maxResults) + 1;

    this.count$.pipe(takeUntil(this.destroy$)).subscribe((count) => {
      this.lastPage = Math.ceil(count / this.maxResults);
    });

    this.isLoading = false;
  }

  addFunction() {
    const modalRef = this.modalService.open(FunctionEditComponent);
    modalRef.componentInstance.function = {
      id: 0,
      name: '',
    };
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: Function) => this.save(functionData));
  }

  editFunction(functionData: Function) {
    const modalRef = this.modalService.open(FunctionEditComponent);
    modalRef.componentInstance.function = functionData;
    modalRef.componentInstance.saveFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: Function) => this.save(functionData));
  }

  save(functionData: Function) {
    this.isLoading = true;
    this.functionsService
      .save(functionData)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function saved successfully.'),
        error: (e) => this.error$.next(e),
        complete: () => this.getFunctions(),
      });
    this.isLoading = false;
  }

  deleteFunction(functionData: Function) {
    const modalRef = this.modalService.open(FunctionDeleteComponent);
    modalRef.componentInstance.function = functionData;
    modalRef.componentInstance.deleteFunction
      .pipe(takeUntil(this.destroy$))
      .subscribe((functionData: Function) => this.delete(functionData));
  }

  delete(functionData: Function) {
    this.isLoading = true;
    this.functionsService
      .delete(functionData.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (r) => this.toastService.success('Function deleted.'),
        error: (e) => this.error$.next(e),
        complete: () => this.getFunctions(),
      });
    this.isLoading = false;
  }

  trackById(index: number, functionData: Function): number {
    return functionData.id;
  }

  setMaxResultsPerPage(max: number) {
    this.maxResults = max;
    this.offset = 0;
    this.getFunctions();
  }

  setNewOffset(offset: number) {
    this.offset = offset;
    this.getFunctions();
  }
}
