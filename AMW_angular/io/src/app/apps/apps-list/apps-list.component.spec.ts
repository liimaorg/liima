import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsListComponent } from './apps-list-component';
import { ComponentRef } from '@angular/core';

describe('AppsListComponent', () => {
  let component: AppsListComponent;
  let componentRef: ComponentRef<AppsListComponent>;
  let fixture: ComponentFixture<AppsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppsListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppsListComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('apps', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
