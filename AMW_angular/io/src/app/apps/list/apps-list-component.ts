import { Component, Input } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { App } from '../app';

@Component({
  selector: 'amw-apps-list',
  standalone: true,
  imports: [AsyncPipe, PaginationComponent],
  templateUrl: './apps-list.component.html',
  styleUrl: './apps-list.component.scss',
})
export class AppsListComponent {
  @Input() apps: App[];
  @Input() even: boolean;
}
