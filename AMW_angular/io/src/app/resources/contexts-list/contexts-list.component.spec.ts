import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextsListComponent } from './contexts-list.component';
import { ActivatedRoute, convertToParamMap, ParamMap } from '@angular/router';
import { of, Subject } from 'rxjs';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

describe('ContextsListComponent', () => {
  let component: ContextsListComponent;
  let fixture: ComponentFixture<ContextsListComponent>;

  const queryParamMap$ = new Subject<ParamMap>();
  const mockRoute: Partial<ActivatedRoute> = { queryParamMap: queryParamMap$ };
  queryParamMap$.next(
    convertToParamMap({
      id: '42',
    }),
  );

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContextsListComponent],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContextsListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
