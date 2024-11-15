import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceTypeAddComponent } from './resource-type-add.component';

describe('ResourceTypeAddComponent', () => {
  let component: ResourceTypeAddComponent;
  let fixture: ComponentFixture<ResourceTypeAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTypeAddComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTypeAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
