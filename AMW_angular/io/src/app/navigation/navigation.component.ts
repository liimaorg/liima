import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-navigation',
  template: `
    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container-fluid">
        <div class="navbar-header">
          <button
            type="button"
            class="navbar-toggle collapsed"
            data-toggle="collapse"
            data-target="#navbar"
            aria-expanded="false"
            aria-controls="navbar"
          >
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <span class="navbar-brand"
            ><img src="assets/images/Liima.png" alt="Liima"
          /></span>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="/AMW_web/pages/applist.xhtml">Apps</a></li>
            <li><a href="/AMW_web/pages/serverListView.xhtml">Servers</a></li>
            <li><a href="/AMW_web/pages/resourceList.xhtml">Resources</a></li>
            <li><a href="#/deployments/">Deploy</a></li>
            <li>
              <a href="/AMW_web/pages/shakedownTest.xhtml">Shakedown Test</a>
            </li>
            <li><a href="/AMW_web/pages/settings.xhtml">Settings</a></li>
            <li><a href="{{ logoutUrl }}">Logout</a></li>
          </ul>
          <form class="navbar-form navbar-right">
            <div class="checkbox">
              <label> <input type="checkbox" /> Favorites only </label>
            </div>
          </form>
        </div>
      </div>
    </nav>
  `,
  styleUrls: ['./navigation.component.scss']
})
export class NavigationComponent {
  @Input()
  logoutUrl: string;
  constructor() {}
}
