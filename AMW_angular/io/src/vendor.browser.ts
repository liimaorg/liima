// For vendors for example jQuery, Lodash, angular2-jwt just import them here unless you plan on
// chunking vendors files for async loading. You would need to import the async loaded vendors
// at the entry point of the async loaded file. Also see custom-typings.d.ts as you also need to
// run `typings install x` where `x` is your module

// TODO(gdi2290): switch to DLLs

// Angular 2
import '@angular/platform-browser';
import '@angular/platform-browser-dynamic';
import '@angular/core';
import '@angular/common';
import '@angular/forms';
import '@angular/http';
import '@angular/router';

// RxJS
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';

// basics
import 'lodash/lodash';
import 'jquery';
import 'moment';

// bootstrap
import 'bootstrap-sass/assets/javascripts/bootstrap/affix';
import 'bootstrap-sass/assets/javascripts/bootstrap/tooltip';
import 'bootstrap-sass/assets/javascripts/bootstrap/popover';
import 'bootstrap-sass/assets/javascripts/bootstrap/transition';

// custom
import 'eonasdan-bootstrap-datetimepicker';
//import 'assets/js/main.min.js';

if ('production' === ENV) {
  // Production
} else {
  // Development
}
