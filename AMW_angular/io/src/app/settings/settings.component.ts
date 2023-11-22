import { Component } from '@angular/core';
import {NavigationStoreService} from "../navigation/navigation-store.service";
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrl: './settings.component.scss',
    standalone: true,
    imports: [RouterOutlet]
})
export class SettingsComponent {
  constructor(public navigationStore: NavigationStoreService) {
    this.navigationStore.setPageTitle('Settings');
    this.navigationStore.setCurrent('Settings');
  }
}
