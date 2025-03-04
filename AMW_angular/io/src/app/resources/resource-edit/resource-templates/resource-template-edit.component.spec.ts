import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceTemplateEditComponent } from './resource-template-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ComponentRef } from '@angular/core';
import { ResourceTemplate } from '../../../resource/resource-template';

describe('ResourceTemplateEditComponent', () => {
  let component: ResourceTemplateEditComponent;
  let componentRef: ComponentRef<ResourceTemplateEditComponent>;
  let fixture: ComponentFixture<ResourceTemplateEditComponent>;
  const template: ResourceTemplate = {
    id: null,
    relatedResourceIdentifier: '',
    name: '',
    targetPath: '',
    targetPlatforms: [''],
    fileContent: '',
    sourceType: 'RESOURCE',
    version: 1,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceTemplateEditComponent],
      providers: [NgbActiveModal, provideHttpClient(withInterceptorsFromDi())],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceTemplateEditComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('template', template);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
