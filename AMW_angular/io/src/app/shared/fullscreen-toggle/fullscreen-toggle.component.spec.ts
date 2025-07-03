import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FullscreenToggleComponent } from './fullscreen-toggle.component';
import { ComponentRef } from '@angular/core';

describe('FullscreenToggleComponent', () => {
  let component: FullscreenToggleComponent;
  let componentRef: ComponentRef<FullscreenToggleComponent>;
  let fixture: ComponentFixture<FullscreenToggleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FullscreenToggleComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FullscreenToggleComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
