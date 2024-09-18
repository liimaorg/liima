import { Component } from '@angular/core';
import { PaginationComponent } from '../../shared/pagination/pagination.component';

@Component({
  selector: 'amw-apps-list',
  standalone: true,
  imports: [PaginationComponent],
  templateUrl: './apps-list.component.html',
  styleUrl: './apps-list.component.scss',
})
export class AppsListComponent {}
