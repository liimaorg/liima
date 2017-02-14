/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation, OnInit, AfterViewInit } from '@angular/core';
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

  constructor(public appState: AppState) {
  }

  ngOnInit() {}

  ngAfterViewInit() {
    $('.navbar-lower').affix({offset:{top:50}});
  }

}
