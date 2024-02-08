import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToastComponent } from '../../shared/elements/toast/toast.component';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';

type Key = { key: string; value: string };

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

  @ViewChild(ToastComponent) toast: ToastComponent;

  constructor(private http: HttpClient) {
    http.get<Key[]>('/AMW_rest/resources/deployments/deploymentParameterKeys').subscribe({
      next: (data) => {
        this.paramKeys = data;
      },
    });
  }

  addKey(): void {
    const trimmedKeyName = this.keyName.trim();
    if (trimmedKeyName.length > 0 && trimmedKeyName.toLowerCase() !== 'null') {
      this.http
        .post<Key>('/AMW_rest/resources/deployments/deploymentParameterKeys', {
          key: this.keyName,
          value: null,
        })
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
    } else {
      this.toast.display('Key name must not be null or empty', 'error');
    }
  }

  deleteKey(keyName: string): void {
    this.http.delete<Key>(`/AMW_rest/resources/deployments/deploymentParameterKeys/${keyName}`).subscribe({
      next: (response) => {
        this.paramKeys = this.paramKeys.filter((key) => key.key !== keyName);
        this.toast.display('Key deleted.');
      },
      error: (error) => {
        this.toast.display(error.error.message, 'error');
      },
    });
  }
}
