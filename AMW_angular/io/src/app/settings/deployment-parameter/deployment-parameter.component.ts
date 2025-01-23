import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { AuthService, isAllowed } from '../../auth/auth.service';
import { Subject } from 'rxjs';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { ButtonComponent } from '../../shared/button/button.component';
import { TableComponent } from '../../shared/table/table.component';

type Key = { id: number; name: string };

@Component({
  selector: 'app-deployment-parameter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, IconComponent, ButtonComponent, TableComponent],
  templateUrl: './deployment-parameter.component.html',
  styleUrl: './deployment-parameter.component.scss',
})
export class DeploymentParameterComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  http = inject(HttpClient);
  toastService = inject(ToastService);

  keyName = '';
  paramKeys: Key[] = [];
  canCreate = signal<boolean>(false);
  canDelete = signal<boolean>(false);
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.getUserPermissions();
    this.http.get<Key[]>('/AMW_rest/resources/deployments/deploymentParameterKeys').subscribe({
      next: (data) => {
        this.paramKeys = data;
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    const actions = this.authService.getActionsForPermission('MANAGE_DEPLOYMENT_PARAMETER');
    this.canCreate.set(actions.some(isAllowed('CREATE')));
    this.canDelete.set(actions.some(isAllowed('DELETE')));
  }

  addKey(): void {
    const trimmedKeyName = this.keyName.trim();
    if (trimmedKeyName.length > 0 && trimmedKeyName.toLowerCase() !== 'null') {
      this.http
        .post<Key>('/AMW_rest/resources/deployments/deploymentParameterKeys', this.keyName)
        .subscribe((newKey) => {
          this.paramKeys.push(newKey);
          this.toastService.success('Key added.');
          this.keyName = '';
        });
    } else {
      this.toastService.error('Key name must not be null or empty');
    }
  }

  deleteKey(keyId: number): void {
    this.http.delete<Key>(`/AMW_rest/resources/deployments/deploymentParameterKeys/${keyId}`).subscribe(() => {
      this.paramKeys = this.paramKeys.filter((key) => key.id !== keyId);
      this.toastService.success('Key deleted.');
    });
  }

  deploymentParameterHeader() {
    return [
      {
        key: 'name',
        title: 'Name',
      },
    ];
  }
}
