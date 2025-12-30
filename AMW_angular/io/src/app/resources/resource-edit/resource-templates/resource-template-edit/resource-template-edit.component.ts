import { Component, computed, EventEmitter, inject, Input, Output, Signal, signal, WritableSignal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ResourceTemplate } from 'src/app/resources/models/resource-template';
import { ResourceTemplatesService } from 'src/app/resources/services/resource-templates.service';
import { ButtonComponent } from 'src/app/shared/button/button.component';
import { CodeEditorComponent } from 'src/app/shared/codemirror/code-editor.component';
import { DiffEditorComponent } from 'src/app/shared/codemirror/diff-editor.component';
import { FullscreenToggleComponent } from 'src/app/shared/fullscreen-toggle/fullscreen-toggle.component';
import { ModalHeaderComponent } from 'src/app/shared/modal-header/modal-header.component';
import { RevisionInformation } from 'src/app/shared/model/revisionInformation';
import { RevisionCompareComponent } from 'src/app/shared/revision-compare/revision-compare.component';

interface TargetPlatformModel {
  name: string;
  selected: boolean;
}

@Component({
  selector: 'app-resource-template-edit',
  templateUrl: './resource-template-edit.component.html',
  styleUrl: './resource-template-edit.component.scss',
  standalone: true,
  imports: [
    CodeEditorComponent,
    FormsModule,
    NgbDropdownModule,
    ModalHeaderComponent,
    ButtonComponent,
    ModalHeaderComponent,
    DiffEditorComponent,
    RevisionCompareComponent,
    FullscreenToggleComponent,
  ],
})
export class ResourceTemplateEditComponent {
  activeModal = inject(NgbActiveModal);

  private _template: ResourceTemplate;
  @Input() set template(value: ResourceTemplate) {
    this._template = value;
    if (value && value.id) {
      this.loadRevisions(value.id);
    }
  }
  get template(): ResourceTemplate {
    return this._template;
  }

  @Input() canAddOrEdit: boolean;

  @Output() saveTemplate: EventEmitter<ResourceTemplate> = new EventEmitter<ResourceTemplate>();

  private templatesService = inject(ResourceTemplatesService);
  allSelectableTargetPlatforms: Signal<string[]> = toSignal(this.templatesService.getAllTargetPlatforms(), {
    initialValue: [],
  });

  public wrapLinesEnabled: false;
  public revisions: WritableSignal<RevisionInformation[]> = signal([]);
  public revision: ResourceTemplate;
  public selectedRevisionName: string;
  public targetPlatformModels: Signal<TargetPlatformModel[]> = computed(() => {
    return this.loadTargetPlatformModelsForTemplate(this.allSelectableTargetPlatforms());
  });
  public revisionTargetPlatformModels: Signal<TargetPlatformModel[]> = computed(() => {
    return this.loadRevisionTargetPlatformModelsForTemplate(this.allSelectableTargetPlatforms());
  });
  public diffValue = {
    original: '',
    modified: '',
  };

  getTitle(): string {
    return (this.template.id ? 'Edit' : 'Add') + ' template';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.isValidForm()) {
      if (this.revision) this.template.fileContent = this.diffValue.original;
      this.saveTemplate.emit(this.template);
      this.activeModal.close();
    }
  }

  toggleFullscreen(isFullscreen: boolean) {
    this.activeModal.update({ fullscreen: isFullscreen });
  }

  loadTargetPlatformModelsForTemplate(allTargetPlatforms: string[]): TargetPlatformModel[] {
    return allTargetPlatforms.map((name) => {
      return {
        name: name,
        selected: this.template.targetPlatforms.includes(name),
      };
    });
  }

  private loadRevisionTargetPlatformModelsForTemplate(allTargetPlatforms: string[]): TargetPlatformModel[] {
    if (!this.revision) return;
    return allTargetPlatforms.map((name) => {
      return {
        name: name,
        selected: this.revision.targetPlatforms.includes(name),
      };
    });
  }

  selectTargetPlatform(targetPlatform: TargetPlatformModel) {
    if (!this.template.targetPlatforms.includes(targetPlatform.name)) {
      this.template.targetPlatforms.push(targetPlatform.name);
    } else {
      const indexOfTargetPlatformNameToRemove = this.template.targetPlatforms.indexOf(targetPlatform.name);
      this.template.targetPlatforms.splice(indexOfTargetPlatformNameToRemove, 1);
    }
  }

  loadRevisions(templateId: number): void {
    this.templatesService.getTemplateRevisions(templateId).subscribe((revisions) => {
      this.revisions.set(revisions);
    });
  }

  selectRevision(revisionId: number, displayName: string): void {
    if (revisionId && displayName) {
      this.templatesService.getTemplateByIdAndRevision(this.template.id, revisionId).subscribe((revision) => {
        this.revision = revision;
        this.selectedRevisionName = displayName;
        this.diffValue = { original: this.template.fileContent, modified: this.revision.fileContent };
      });
    } else {
      //reset selected revision
      this.revision = null;
      this.selectedRevisionName = null;
    }
  }

  isValidForm(): boolean {
    return this.isNameValid() && this.isValidTargetPath();
  }

  isNameValid(): boolean {
    const REGEXP_NON_EMPTY_TRIMMED = /^\S(.*\S)?$/;
    return this.template ? REGEXP_NON_EMPTY_TRIMMED.test(this.template.name) : false;
  }

  isValidTargetPath() {
    const REGEXP_FILE_PATH_PATTERN = /^(?![\w]:|\/|\.\.)(?!.*\.\.)(?=.*\S)[^\s].*[^\s]$|^$/;
    return this.template && this.template.targetPath && this.template.targetPath.trim() !== ''
      ? REGEXP_FILE_PATH_PATTERN.test(this.template.targetPath)
      : false;
  }
}
