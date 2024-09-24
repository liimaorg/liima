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

  addFunction() {}

  editFunction() {}

  saveFunction() {}

  deleteFunction() {}
}
