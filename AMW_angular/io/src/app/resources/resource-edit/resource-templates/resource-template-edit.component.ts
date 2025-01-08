import { Component, EventEmitter, inject, Input, Output, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { CodeEditorComponent } from '../../../shared/codemirror/code-editor.component';
import { DiffEditorComponent } from '../../../shared/codemirror/diff-editor.component';
import { ResourceTemplate } from '../../../resource/resource-template';

@Component({
  selector: 'app-resource-template-edit',
  templateUrl: './resource-template-edit.component.html',
  styleUrl: './resource-template-edit.component.scss',
  standalone: true,
  imports: [
    CodeEditorComponent,
    DiffEditorComponent,
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
export class ResourceTemplateEditComponent {
  @Input() template: ResourceTemplate;
  @Input() canEdit: boolean;
  @Input() isOverwrite: boolean;

  @Output() saveTemplate: EventEmitter<ResourceTemplate> = new EventEmitter<ResourceTemplate>();

  constructor(public activeModal: NgbActiveModal) {}

  getTitle(): string {
    return (this.template.id ? (this.isOverwrite ? 'Overwrite' : 'Edit') : 'Add') + ' template';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    this.saveTemplate.emit(this.template);
    this.activeModal.close();
  }
}
