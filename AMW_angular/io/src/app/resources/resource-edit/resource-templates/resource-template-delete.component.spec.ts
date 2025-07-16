import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceTemplateDeleteComponent } from './resource-template-delete.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('ResourceTemplateDeleteComponent', () => {
  let component: ResourceTemplateDeleteComponent;
  let fixture: ComponentFixture<ResourceTemplateDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTemplateDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTemplateDeleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
