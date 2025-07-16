import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RevisionCompareComponent } from './revision-compare.component';
import { ComponentRef } from '@angular/core';

describe('RevisionCompareComponent', () => {
  let component: RevisionCompareComponent;
  let componentRef: ComponentRef<RevisionCompareComponent>;
  let fixture: ComponentFixture<RevisionCompareComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RevisionCompareComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RevisionCompareComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('revisions', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
