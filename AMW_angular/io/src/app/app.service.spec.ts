import { inject, TestBed } from '@angular/core/testing';
// Load the implementations that should be tested
import { AppState } from './app.service';

describe('AppService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      AppState
    ]
  }));

  it('should return an empty if no navItems have been set',
    inject([AppState], (appState: AppState) => {
      expect(appState.navItems()).toEqual([]);
  }));

  it('should return an array of navItems if navItems have been set',
    inject([AppState], (appState: AppState) => {
      // given
      const items: any[] = [{title: 'aTest', target: '/aTarget'}, {title: 'bTest', target: '/bTarget'}];
      appState.set('navItems', items);
      // when then
      expect(appState.navItems()).toEqual(items);
  }));

});
