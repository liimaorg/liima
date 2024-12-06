import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourcesListComponent } from './resources-list.component';
import { ComponentRef } from '@angular/core';
import { Resource } from '../../resource/resource';
import { ResourceType } from '../../resource/resource-type';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ResourceEditComponent } from './resource-edit.component';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';

describe('ResourceEditComponent', () => {
  let component: ResourceEditComponent;
  let componentRef: ComponentRef<ResourceEditComponent>;
  let fixture: ComponentFixture<ResourceEditComponent>;

  const mockRoute: any = { paramMap: of() };
  mockRoute.paramMap = new Subject<any>();
  mockRoute.paramMap.next({
    id: 42,
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceEditComponent, RouterTestingModule.withRoutes([])],
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: mockRoute },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceEditComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
