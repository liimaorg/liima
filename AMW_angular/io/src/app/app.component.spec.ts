import { inject, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
// Load the implementations that should be tested
import { AppComponent } from './app.component';
import { AppState } from './app.service';

describe('App', () => {
  // provide our implementations or mocks to the dependency injector
  beforeEach(() => TestBed.configureTestingModule({
    imports: [ RouterTestingModule ],
    providers: [
      AppState,
      AppComponent
    ]
  }));

  it('should have a name', inject([AppComponent], (app: AppComponent) => {
    expect(app.name).toEqual('Angular 2');
  }));

});
