import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ToastComponent } from '../../shared/elements/toast/toast.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ToastComponent, RouterLink],
  template: `
    <div class="row">
      <div class="col-12 bg-dark-subtle pt-2">
        <h4><ng-content select=".page-title"></ng-content></h4>
      </div>
    </div>
    <div class="container mt-4">
      <ng-content select=".page-content"></ng-content>
    </div>
  `,
  styles: ``,
})
export class PageComponent {}
