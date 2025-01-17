import {
  Component,
  EventEmitter,
  inject,
  Input,
  Output,
  OnInit,
  Signal,
  computed,
  WritableSignal,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { CodeEditorComponent } from '../../../shared/codemirror/code-editor.component';
import { ResourceTemplate } from '../../../resource/resource-template';
import { ResourceTemplatesService } from '../../../resource/resource-templates.service';
import { Resource } from '../../../resource/resource';
import { RevisionInformation } from '../../../shared/model/revisionInformation';
import { DiffEditorComponent } from '../../../shared/codemirror/diff-editor.component';

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
    CommonModule,
    IconComponent,
    NgbDropdownModule,
    ModalHeaderComponent,
    ButtonComponent,
    ModalHeaderComponent,
    IconComponent,
    DiffEditorComponent,
  ],
})
export class ResourceTemplateEditComponent implements OnInit {
  @Input() template: ResourceTemplate;
  @Input() resource: Resource;
  @Input() canEdit: boolean;
  @Input() isOverwrite: boolean;
  @Input() contextId: number;

  @Output() saveTemplate: EventEmitter<ResourceTemplate> = new EventEmitter<ResourceTemplate>();

  private templatesService = inject(ResourceTemplatesService);
  allSelectableTargetPlatforms: Signal<string[]> = this.templatesService.allTargetPlatformsByContextId;

  public revisions: RevisionInformation[] = [];
  public revision: ResourceTemplate;
  public selectedRevisionName: WritableSignal<string> = signal(null);
  public isFullscreen = false;
  public toggleFullscreenIcon = 'arrows-fullscreen';
  public targetPlatformModels: Signal<TargetPlatformModel[]> = computed(() => {
    return this.loadTargetPlatformModelsForTemplate(this.allSelectableTargetPlatforms());
  });
  public revisionTargetPlatformModels: Signal<TargetPlatformModel[]> = computed(() => {
    return this.loadRevisionTargetPlatformModelsForTemplate(
      this.allSelectableTargetPlatforms(),
      this.selectedRevisionName(),
    );
  });
  public diffValue = {
    original: '',
    modified: '',
  };

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {
    if (this.contextId) {
      this.templatesService.setContexIdForAllTargetPlatforms(this.contextId);
    }
    if (this.template && this.template.id) {
      this.loadRevisions(this.template.id);
    }
  }

  getTitle(): string {
    return (this.template.id ? (this.isOverwrite ? 'Overwrite' : 'Edit') : 'Add') + ' template';
  }

  cancel() {
    this.activeModal.close();
    this.templatesService.setIdForResourceTemplateList(this.resource.id);
  }

  save() {
    if (this.isValidForm()) {
      this.saveTemplate.emit(this.template);
      this.activeModal.close();
    }
  }

  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    this.toggleFullscreenIcon = this.isFullscreen ? 'fullscreen-exit' : 'arrows-fullscreen';
    this.activeModal.update({ fullscreen: this.isFullscreen });
  }

  loadTargetPlatformModelsForTemplate(allTargetPlatforms: string[]): TargetPlatformModel[] {
    return allTargetPlatforms.map((name) => {
      return {
        name: name,
        selected: this.template.targetPlatforms.includes(name),
      };
    });
  }

  private loadRevisionTargetPlatformModelsForTemplate(allTargetPlatforms: string[], s: string): TargetPlatformModel[] {
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
      this.revisions = revisions;
    });
  }

  selectRevision(templateId: number, revisionId: number, displayName: string): void {
    this.templatesService.getTemplateByIdAndRevision(templateId, revisionId).subscribe((revision) => {
      this.revision = revision;
      this.selectedRevisionName.update((value) => displayName);
      this.diffValue = { original: this.template.fileContent, modified: this.revision.fileContent };
    });
  }

  isValidForm(): boolean {
    return this.isNameValid() && this.isValidTargetPath();
  }

  isNameValid(): boolean {
    const REGEXP_NON_EMPTY_TRIMMED = /^\S(.*\S)?$/;
    return this.template ? REGEXP_NON_EMPTY_TRIMMED.test(this.template.name) : false;
  }

  isValidTargetPath() {
    const REGEXP_FILE_PATH_PATTERN = /^(?!\/)(?!\.\.\/).*/;
    return this.template ? REGEXP_FILE_PATH_PATTERN.test(this.template.targetPath) : false;
  }
}
