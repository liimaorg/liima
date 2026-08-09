import { Component, input, inject } from '@angular/core';
import { App } from '../app';
import { Router } from '@angular/router';

@Component({
  selector: 'app-apps-list',
  templateUrl: './apps-list.component.html',
  styleUrl: './apps-list.component.scss',
})
export class AppsListComponent {
  apps = input.required<App[]>();
  private router = inject(Router);

  navigateToResource(id: number) {
    this.router.navigate(['/resource/edit'], { queryParams: { id, ctx: 1 } });
  }
}
