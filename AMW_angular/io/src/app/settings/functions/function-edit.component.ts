import { Component, EventEmitter, inject, Input, Output, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { AppFunction } from './appFunction';
//import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { FunctionsService } from './functions.service';
import { RevisionInformation } from './revisionInformation';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../../shared/button/button.component';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';

@Component({
  selector: 'app-function-edit',
  templateUrl: './function-edit.component.html',
  standalone: true,
  imports: [FormsModule, CommonModule, NgbDropdownModule, ModalHeaderComponent, ButtonComponent],
})
export class FunctionEditComponent implements OnInit {
  @Input() function: AppFunction;
  @Input() canManage: boolean;
  @Output() saveFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  private functionsService = inject(FunctionsService);
  public revisions: RevisionInformation[] = [];
  private revision: AppFunction;
  public selectedRevisionName: string;

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {
    if (this.function && this.function.id) {
      this.loadRevisions(this.function.id);
    }
  }

  getTitle(): string {
    return this.function.id ? 'Edit function' : 'Add function';
  }

  cancel() {
    this.activeModal.close();
    this.functionsService.refreshData();
  }

  save() {
    this.saveFunction.emit(this.function);
    this.activeModal.close();
  }

  loadRevisions(functionId: number): void {
    this.functionsService.getFunctionRevisions(functionId).subscribe((revisions) => {
      this.revisions = revisions;
    });
  }

  selectRevision(functionId: number, revisionId: number, displayName: string): void {
    this.functionsService.getFunctionByIdAndRevision(functionId, revisionId).subscribe((revision) => {
      this.revision = revision;
      this.selectedRevisionName = displayName;
    });
  }

  onChange(content: string) {
    this.function = { ...this.function, content: content };
  }
}
