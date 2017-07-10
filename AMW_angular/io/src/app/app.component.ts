/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation, OnInit, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { AppState } from './app.service';

declare var $: any;

/*
 * App Component
 * Top Level Component
 */
@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: [
    './app.component.scss'
  ],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, AfterViewInit {
  name = 'Angular 2';

  constructor(public appState: AppState,
              private router: Router) {
  }

  ngOnInit() {}

  ngAfterViewInit() {
    $('.navbar-lower').affix({offset: {top: 50}});
  }

  navigateTo(item: any) {
    this.appState.set('navTitle', item.title);
    this.router.navigateByUrl(item.target);
  }

}
