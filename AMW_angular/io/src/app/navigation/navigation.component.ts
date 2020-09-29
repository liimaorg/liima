import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-navigation',
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-0">
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
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div
        class="collapse navbar-collapse bg-dark justify-content-end"
        id="navbarSupportedContent"
      >
        <ul class="navbar-nav float-right">
          <form class="form-inline navbar-form navbar-right">
            <label>
              <input class="m-0 mr-2" type="checkbox" />
              <span class="nav-link font-weight-light">Favorites only</span>
            </label>
          </form>
          <li class="nav-item">
            <a class="nav-link" href="/AMW_web/pages/applist.xhtml">Apps</a>
          </li>
          <li class="nav-item"></li>
          <li>
            <a class="nav-link" href="/AMW_web/pages/serverListView.xhtml"
              >Servers</a
            >
          </li>

          <li class="nav-item">
            <a class="nav-link" href="/AMW_web/pages/resourceList.xhtml"
              >Resources</a
            >
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#/deployments/">Deploy</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/AMW_web/pages/shakedownTest.xhtml"
              >Shakedown Test</a
            >
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/AMW_web/pages/settings.xhtml"
              >Settings</a
            >
          </li>
          <li class="nav-item">
            <a class="nav-link" href="{{ logoutUrl }}">Logout</a>
          </li>
        </ul>
      </div>
    </nav>
  `,
  styleUrls: ['./navigation.component.scss'],
})
export class NavigationComponent {
  @Input()
  logoutUrl: string;
  constructor() {}
}
