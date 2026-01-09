import { Component, EventEmitter, inject, Input, Output, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { AppFunction } from './appFunction';
import { FunctionsService } from './functions.service';
import { RevisionInformation } from '../../shared/model/revisionInformation';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../../shared/button/button.component';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';
import { DiffEditorComponent } from '../../shared/codemirror/diff-editor.component';
import { RevisionCompareComponent } from '../../shared/revision-compare/revision-compare.component';
import { FullscreenToggleComponent } from '../../shared/fullscreen-toggle/fullscreen-toggle.component';

@Component({
  selector: 'app-function-edit',
  templateUrl: './function-edit.component.html',
  styleUrl: './function-edit.component.scss',
  imports: [
    CodeEditorComponent,
    DiffEditorComponent,
    FormsModule,
    NgbDropdownModule,
    ModalHeaderComponent,
    ButtonComponent,
    RevisionCompareComponent,
    FullscreenToggleComponent,
  ],
})
export class FunctionEditComponent {
  activeModal = inject(NgbActiveModal);

  private _function: AppFunction;
  @Input() set function(value: AppFunction) {
    this._function = value;
    if (value && value.id) {
      this.loadRevisions(value.id);
    }
  }
  get function(): AppFunction {
    return this._function;
  }

  @Input() canManage: boolean;
  @Output() saveFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  public wrapLinesEnabled: false;
  private functionsService = inject(FunctionsService);
  public revisions: WritableSignal<RevisionInformation[]> = signal([]);
  public revision: WritableSignal<AppFunction | null> = signal(null);
  public selectedRevisionName: WritableSignal<string | null> = signal(null);

  public diffValue = {
    original: '',
    modified: '',
  };

  getTitle(): string {
    return this.function.id ? 'Edit function' : 'Add function';
  }

  cancel() {
    this.activeModal.close();
    this.functionsService.refreshData();
  }

  hasInvalidFields(): boolean {
    return this.function.name.length === 0 || this.function.content.length === 0;
  }

  save() {
    if (this.hasInvalidFields()) {
      document.querySelectorAll('.needs-validation')[0].classList.add('was-validated');
      return;
    }
    if (this.revision()) this.function.content = this.diffValue.original;
    this.saveFunction.emit(this.function);
    this.activeModal.close();
  }

  loadRevisions(functionId: number): void {
    this.functionsService.getFunctionRevisions(functionId).subscribe({
      next: (revisions) => {
        this.revisions.set(revisions);
      },
      error: (err) => {
        console.error('Failed to load revisions:', err);
        this.revisions.set([]);
      },
    });
  }

  selectRevision(revisionId: number, displayName: string): void {
    if (revisionId && displayName) {
      this.functionsService.getFunctionByIdAndRevision(this.function.id, revisionId).subscribe((revision) => {
        this.revision.set(revision);
        this.selectedRevisionName.set(displayName);
        this.diffValue = { original: this.function.content, modified: revision.content };
      });
    } else {
      //reset selected revision
      this.revision.set(null);
      this.selectedRevisionName.set(null);
    }
  }

  toggleFullscreen(isFullscreen: boolean) {
    this.activeModal.update({ fullscreen: isFullscreen });
  }
}
