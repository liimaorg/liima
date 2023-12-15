import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { PageComponent } from '../layout/page/page.component';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.scss',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, PageComponent],
})
export class SettingsComponent {
  constructor() {}
}
