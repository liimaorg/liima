import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppsFilterComponent } from './apps-filter.component';
import { InputSignal, signal } from '@angular/core';
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
    component.releases = signal<Release[]>([]) as unknown as InputSignal<Release[]>;
    component.releaseId = signal<number>(0) as unknown as InputSignal<number>;
    component.filter = signal<string>('') as unknown as InputSignal<string>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
