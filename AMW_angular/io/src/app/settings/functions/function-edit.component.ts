import { Component, EventEmitter, inject, Input, Output, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { AppFunction } from './appFunction';
import { FunctionsService } from './functions.service';
import { RevisionInformation } from '../../shared/model/revisionInformation';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ButtonComponent } from '../../shared/button/button.component';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';
import { DiffEditorComponent } from '../../shared/codemirror/diff-editor.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { RevisionCompareComponent } from '../../shared/revision-compare/revision-compare.component';
import {FullscreenToggleComponent} from "../../shared/fullscreen-toggle/fullscreen-toggle.component";

@Component({
  selector: 'app-function-edit',
  templateUrl: './function-edit.component.html',
  styleUrl: './function-edit.component.scss',
  imports: [
    CodeEditorComponent,
    DiffEditorComponent,
    FormsModule,
    CommonModule,
    IconComponent,
    NgbDropdownModule,
    ModalHeaderComponent,
    ButtonComponent,
    RevisionCompareComponent,
    FullscreenToggleComponent,
  ],
})
export class FunctionEditComponent implements OnInit {
  @Input() function: AppFunction;
  @Input() canManage: boolean;
  @Output() saveFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  private functionsService = inject(FunctionsService);
  public revisions: RevisionInformation[] = [];
  public revision: AppFunction;
  public selectedRevisionName: string;
  public isFullscreen = false;
  public toggleFullscreenIcon = 'arrows-fullscreen';

  public diffValue = {
    original: '',
    modified: '',
  };

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

  hasInvalidFields(): boolean {
    return this.function.name.length === 0 || this.function.content.length === 0;
  }

  save() {
    if (this.hasInvalidFields()) {
      document.querySelectorAll('.needs-validation')[0].classList.add('was-validated');
      return;
    }
    if (this.revision) this.function.content = this.diffValue.original;
    this.saveFunction.emit(this.function);
    this.activeModal.close();
  }

  loadRevisions(functionId: number): void {
    this.functionsService.getFunctionRevisions(functionId).subscribe((revisions) => {
      this.revisions = revisions;
    });
  }

  selectRevision(revisionId: number, displayName: string): void {
    if (revisionId && displayName) {
      this.functionsService.getFunctionByIdAndRevision(this.function.id, revisionId).subscribe((revision) => {
        this.revision = revision;
        this.selectedRevisionName = displayName;
        this.diffValue = { original: this.function.content, modified: this.revision.content };
      });
    } else {
      //reset selected revision
      this.revision = null;
      this.selectedRevisionName = null;
    }
  }

  toggleFullscreen(isFullscreen: boolean) {
    this.activeModal.update({ fullscreen: isFullscreen });
  }
}
