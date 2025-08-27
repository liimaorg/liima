import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ResourceTemplate } from 'src/app/resources/models/resource-template';
import { ResourceTemplateEditComponent } from './resource-template-edit.component';

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

  describe('isValidTargetPath', () => {
    it('should return false if template is null', () => {
      component.template = null as any;
      expect(component.isValidTargetPath()).toBe(false);
    });

    it('should return false if template.targetPath is null', () => {
      component.template.targetPath = null;
      expect(component.isValidTargetPath()).toBe(false);
    });

    it('should return false if template.targetPath is empty string', () => {
      component.template.targetPath = '';
      expect(component.isValidTargetPath()).toBe(false);
    });

    it('should return false if template.targetPath is only whitespaces', () => {
      component.template.targetPath = '     ';
      expect(component.isValidTargetPath()).toBe(false);
    });

    it('should return true for a valid file path', () => {
      component.template.targetPath = 'file.txt';
      expect(component.isValidTargetPath()).toBe(true);

      component.template.targetPath = 'valid-file.tmp';
      expect(component.isValidTargetPath()).toBe(true);

      component.template.targetPath = 'folder/file.txt';
      expect(component.isValidTargetPath()).toBe(true);

      component.template.targetPath = 'folder/subfolder/file';
      expect(component.isValidTargetPath()).toBe(true);

      component.template.targetPath = 'configuration/some-mapping.properties';
      expect(component.isValidTargetPath()).toBe(true);

      component.template.targetPath = 'run_app.conf';
      expect(component.isValidTargetPath()).toBe(true);

      component.template.targetPath = 'modules_jparc';
      expect(component.isValidTargetPath()).toBe(true);
    });

    it('should return false for an invalid targetPath', () => {
      component.template.targetPath = '/absolute.txt';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = '/absolute/path.txt';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = '../traversals.txt';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = '../traversals/path.txt';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = 'folder/../subfolder/file.txt';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = '   folder/subfolder/file';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = 'folder/subfolder/file     ';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = 'D:file.txt';
      expect(component.isValidTargetPath()).toBe(false);

      component.template.targetPath = 'C:\\windows\\file.txt';
      expect(component.isValidTargetPath()).toBe(false);
    });
  });
});
