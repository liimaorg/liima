import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RelatedResourcesListComponent } from './related-resources-list.component';

describe('RelatedResourcesListComponent', () => {
  let component: RelatedResourcesListComponent;
  let fixture: ComponentFixture<RelatedResourcesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RelatedResourcesListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RelatedResourcesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
