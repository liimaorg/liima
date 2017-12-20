/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation, OnInit, AfterViewInit, ChangeDetectorRef, AfterViewChecked } from '@angular/core';
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
export class AppComponent implements OnInit, AfterViewInit, AfterViewChecked {
  name = 'Angular 4';

  constructor(public appState: AppState,
              private router: Router,
              private cdRef: ChangeDetectorRef) {
  }

  ngOnInit() {}

  ngAfterViewChecked() {
    // explicit change detection to avoid "expression-has-changed-after-it-was-checked-error"
    this.cdRef.detectChanges();
  }

  ngAfterViewInit() {
    $('.navbar-lower').affix({offset: {top: 50}});
  }

  navigateTo(item: any) {
    this.appState.set('navTitle', item.title);
    this.router.navigateByUrl(item.target);
  }

}
