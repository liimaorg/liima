import { Component, input } from '@angular/core';
import { App } from '../app';

@Component({
    selector: 'app-apps-list',
    imports: [],
    templateUrl: './apps-list.component.html',
    styleUrl: './apps-list.component.scss'
})
export class AppsListComponent {
  apps = input.required<App[]>();
  even = input.required<boolean>();
}
