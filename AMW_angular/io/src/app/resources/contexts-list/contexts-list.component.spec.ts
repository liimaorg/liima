import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextsListComponent } from './contexts-list.component';

describe('ContextsListComponent', () => {
  let component: ContextsListComponent;
  let fixture: ComponentFixture<ContextsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContextsListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ContextsListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
