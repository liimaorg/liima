import { Component, EventEmitter, inject, Input, Output, OnInit, Signal, computed } from '@angular/core';
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

  public isFullscreen = false;
  public toggleFullscreenIcon = 'arrows-fullscreen';
  public targetPlatformModels: Signal<TargetPlatformModel[]> = computed(() => {
    return this.loadTargetPlatformModelsForTemplate(this.allSelectableTargetPlatforms());
  });

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {
    if (this.contextId) {
      this.templatesService.setContexIdForAllTargetPlatforms(this.contextId);
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
    this.saveTemplate.emit(this.template);
    this.activeModal.close();
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

  selectTargetPlatform(targetPlatform: TargetPlatformModel) {
    if (!this.template.targetPlatforms.includes(targetPlatform.name)) {
      this.template.targetPlatforms.push(targetPlatform.name);
    } else {
      const indexOfTargetPlatformNameToRemove = this.template.targetPlatforms.indexOf(targetPlatform.name);
      this.template.targetPlatforms.splice(indexOfTargetPlatformNameToRemove, 1);
    }
  }
}
