import { inject, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
// Load the implementations that should be tested
import { AppComponent } from './app.component';
import { AppState } from './app.service';

class RouterStub {
  navigateByUrl( url: string ) { return url; }
}

describe('App', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [ RouterTestingModule ],
    providers: [
      AppState,
      AppComponent,
      { provide: Router, useClass: RouterStub }
    ]
  }));

  it('should have a name', inject([AppComponent], (app: AppComponent) => {
    expect(app.name).toEqual('Angular 2');
  }));

  it('should navigate to the right target',
    inject([AppComponent, AppState, Router], (app: AppComponent, appState: AppState, router: Router) => {
    // given
    const item: any = {title: 'test', target: 'target'};
    // when
    spyOn(appState, 'set').and.callThrough();
    spyOn(router, 'navigateByUrl').and.callThrough();
    app.navigateTo(item);
    // then
    expect(appState.set).toHaveBeenCalledWith('navTitle', 'test');
    expect(router.navigateByUrl).toHaveBeenCalledWith('target');
  }));

});
