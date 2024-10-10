import { Component, Input } from '@angular/core';
import { NgbCollapse } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-navigation',
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark sticky-top bg-dark mb-0">
      <div class="container-fluid">
        <span class="navbar-brand">
          <img src="assets/images/Liima.svg" alt="Liima" width="80px" />
        </span>

        <button
          class="navbar-toggler"
          type="button"
          data-toggle="collapse"
          data-target="#navbarSupportedContent"
          aria-controls="navbarSupportedContent"
          aria-expanded="false"
          aria-label="Toggle navigation"
          (click)="isMenuCollapsed = !isMenuCollapsed"
        >
          <span class="navbar-toggler-icon"></span>
        </button>

        <div
          [ngbCollapse]="isMenuCollapsed"
          class="collapse navbar-collapse bg-dark justify-content-lg-end"
          id="navbarSupportedContent"
        >
          <ul class="navbar-nav p-3 p-lg-0">
            <li class="nav-item">
              <a class="nav-link" href="#/apps/">Apps</a>
            </li>
            <li class="nav-item"></li>
            <li>
              <a class="nav-link" href="/AMW_web/pages/serverListView.xhtml">Servers</a>
            </li>

            <li class="nav-item">
              <a class="nav-link" href="/AMW_web/pages/resourceList.xhtml">Resources</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#/deployments/">Deploy</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/AMW_web/pages/settings.xhtml">Settings</a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="{{ logoutUrl }}">Logout</a>
            </li>
          </ul>
        </div>
      </div>
    </nav>
  `,
  styleUrls: ['./navigation.component.scss'],
  standalone: true,
  imports: [NgbCollapse],
})
export class NavigationComponent {
  @Input()
  logoutUrl: string;
  public isMenuCollapsed = true;
  constructor() {}
}
