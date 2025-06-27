import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsServersListComponent } from './apps-servers-list.component';
import { ComponentRef } from '@angular/core';

describe('AppsListComponent', () => {
  let component: AppsServersListComponent;
  let componentRef: ComponentRef<AppsServersListComponent>;
  let fixture: ComponentFixture<AppsServersListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppsServersListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppsServersListComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('appServers', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
