import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceAddComponent } from './resource-add.component';

describe('ResourceAddComponent', () => {
  let component: ResourceAddComponent;
  let fixture: ComponentFixture<ResourceAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceAddComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
