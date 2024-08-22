import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service';
import { Subject } from 'rxjs';
import { ToastService } from '../../shared/elements/toast/toast.service';

type Key = { id: number; name: string };

@Component({
  selector: 'app-deployment-parameter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, IconComponent],
  templateUrl: './deployment-parameter.component.html',
  styleUrl: './deployment-parameter.component.scss',
})
export class DeploymentParameterComponent implements OnInit, OnDestroy {
  keyName = '';
  paramKeys: Key[] = [];
  canCreate = signal<boolean>(false);
  canDelete = signal<boolean>(false);
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private toastService: ToastService,
  ) {}

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
    this.authService.getActionsForPermission('MANAGE_DEPLOYMENT_PARAMETER').map((action) => {
      if (action.indexOf('ALL') > -1) {
        this.canDelete.set(true);
        this.canCreate.set(true);
      } else {
        this.canCreate.set(action.indexOf('CREATE') > -1);
        this.canDelete.set(action.indexOf('DELETE') > -1);
      }
    });
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
}
