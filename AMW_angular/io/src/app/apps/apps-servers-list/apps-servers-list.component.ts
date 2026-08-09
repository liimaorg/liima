import { Component, input, inject } from '@angular/core';
import { AppServer } from '../app-server';
import { AppsListComponent } from '../apps-list/apps-list-component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-apps-servers-list',
  imports: [AppsListComponent],
  templateUrl: './apps-servers-list.component.html',
  styleUrl: './apps-servers-list.component.scss',
})
export class AppsServersListComponent {
  appServers = input.required<AppServer[]>();
  private router = inject(Router);

  navigateToResource(id: number) {
    this.router.navigate(['/resource/edit'], { queryParams: { id, ctx: 1 } });
  }
}
