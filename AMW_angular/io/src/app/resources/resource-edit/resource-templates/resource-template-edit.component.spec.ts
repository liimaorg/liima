import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceTemplateEditComponent } from './resource-template-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ResourceTemplateEditComponent', () => {
  let component: ResourceTemplateEditComponent;
  let fixture: ComponentFixture<ResourceTemplateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTemplateEditComponent],
      providers: [NgbActiveModal, provideHttpClient(withInterceptorsFromDi())],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTemplateEditComponent);
    component = fixture.componentInstance;

    component.template = {
      id: null,
      relatedResourceIdentifier: '',
      name: '',
      targetPath: '',
      targetPlatforms: [''],
      fileContent: '',
      sourceType: 'RESOURCE',
      version: 1,
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
