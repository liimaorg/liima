import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastComponent } from '../../shared/elements/toast/toast.component';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service';
import { Subject } from 'rxjs';

type Key = { id: number; name: string };

@Component({
  selector: 'app-deployment-parameter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ToastComponent, FormsModule, IconComponent],
  templateUrl: './deployment-parameter.component.html',
  styleUrl: './deployment-parameter.component.scss',
})
export class DeploymentParameterComponent {
  keyName = '';
  paramKeys: Key[] = [];
  canCreate: boolean = false;
  canDelete: boolean = false;
  private destroy$ = new Subject<void>();

  @ViewChild(ToastComponent) toast: ToastComponent;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {
    this.getUserPermissions();
    http.get<Key[]>('/AMW_rest/resources/deployments/deploymentParameterKeys').subscribe({
      next: (data) => {
        this.paramKeys = data;
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    this.authService
      .getActionsForPermission('MANAGE_DEPLOYMENT_PARAMETER')
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        if (value.indexOf('ALL') > -1) {
          this.canCreate = this.canDelete = true;
        } else {
          this.canCreate = value.indexOf('CREATE') > -1;
          this.canDelete = value.indexOf('DELETE') > -1;
        }
      });
  }

  addKey(): void {
    const trimmedKeyName = this.keyName.trim();
    if (trimmedKeyName.length > 0 && trimmedKeyName.toLowerCase() !== 'null') {
      this.http.post<Key>('/AMW_rest/resources/deployments/deploymentParameterKeys', this.keyName).subscribe({
        next: (newKey) => {
          this.paramKeys.push(newKey);
          this.toast.display('Key added.');
          this.keyName = '';
        },
        error: (error) => {
          this.toast.display(error.error.message, 'error', 5000);
        },
      });
    } else {
      this.toast.display('Key name must not be null or empty', 'error', 5000);
    }
  }

  deleteKey(keyId: number): void {
    this.http.delete<Key>(`/AMW_rest/resources/deployments/deploymentParameterKeys/${keyId}`).subscribe({
      next: (response) => {
        this.paramKeys = this.paramKeys.filter((key) => key.id !== keyId);
        this.toast.display('Key deleted.');
      },
      error: (error) => {
        this.toast.display(error.error.message, 'error', 5000);
      },
    });
  }
}
