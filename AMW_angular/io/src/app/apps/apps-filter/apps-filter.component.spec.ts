import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsFilterComponent } from './apps-filter.component';
import { input } from '@angular/core';
import { Release } from '../../settings/releases/release';

describe('AppsFilterComponent', () => {
  let component: AppsFilterComponent;
  let fixture: ComponentFixture<AppsFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppsFilterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppsFilterComponent);
    component = fixture.componentInstance;
    component.releases = input<Release[]>([]);
    component.releaseId = input<number>(0);
    component.filter = input<string | undefined>('');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
