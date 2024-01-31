import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastComponent } from '../../shared/elements/toast/toast.component';
import { HttpClient } from '@angular/common/http';

type Key = { key: string; value: any };

@Component({
  selector: 'app-deployment-parameter',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ToastComponent, FormsModule],
  templateUrl: './deployment-parameter.component.html',
  styleUrl: './deployment-parameter.component.scss',
})
export class DeploymentParameterComponent {
  keyName = '';
  paramKeys: Key[] = [];

  @ViewChild(ToastComponent) toast: ToastComponent;

  constructor(private http: HttpClient) {
    http.get<Key[]>('/AMW_rest/resources/deployments/deploymentParameterKeys/').subscribe({
      next: (data) => {
        this.paramKeys = data;
      },
    });
  }

  addKey(): void {
    if (this.keyName.trim().length > 0) {
      this.http
        .post<Key>('/AMW_rest/resources/deployments/deploymentParameterKeys/', { key: this.keyName, value: null })
        .subscribe({
          next: (newKey) => {
            this.paramKeys.push(newKey);
            this.toast.display('Key added.');
            this.keyName = '';
          },
          error: (error) => {
            this.toast.display(error.error.message, 'error');
          },
        });
    }
  }

  deleteKey(keyId: number): void {
    this.toast.display('Dummy action: Key would be deleted.');
  }
}
