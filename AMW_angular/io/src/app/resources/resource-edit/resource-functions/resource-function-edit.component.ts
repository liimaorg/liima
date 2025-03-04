import { Component, EventEmitter, inject, Input, Output, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ResourceFunctionsService } from '../../resource-functions.service';
import { RevisionInformation } from '../../../shared/model/revisionInformation';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { IconComponent } from '../../../shared/icon/icon.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { CodeEditorComponent } from '../../../shared/codemirror/code-editor.component';
import { DiffEditorComponent } from '../../../shared/codemirror/diff-editor.component';
import { ResourceFunction } from '../../resource-function';
import { RevisionCompareComponent } from '../../../shared/revision-compare/revision-compare.component';

@Component({
  selector: 'app-resource-function-edit',
  templateUrl: './resource-function-edit.component.html',
  styleUrl: './resource-function-edit.component.scss',
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
    RevisionCompareComponent,
  ],
})
export class ResourceFunctionEditComponent implements OnInit {
  @Input() function: ResourceFunction;
  @Input() canEdit: boolean;
  @Input() isOverwrite: boolean;

  @Output() saveFunction: EventEmitter<ResourceFunction> = new EventEmitter<ResourceFunction>();

  private functionsService = inject(ResourceFunctionsService);
  public revisions: RevisionInformation[] = [];
  public revision: ResourceFunction;
  public selectedRevisionName: string;
  public isFullscreen = false;
  public toggleFullscreenIcon = 'arrows-fullscreen';
  public newMik: string = '';
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
    return (this.function.id ? (this.isOverwrite ? 'Overwrite' : 'Edit') : 'Add') + ' function';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.revision) this.function.content = this.diffValue.original;
    if (this.newMik !== '') this.addMik();
    this.saveFunction.emit(this.function);
    this.activeModal.close();
  }

  loadRevisions(functionId: number): void {
    this.functionsService.getFunctionRevisions(functionId).subscribe((revisions) => {
      this.revisions = revisions;
    });
  }

  selectRevision(revisionId: number, displayName: string): void {
    this.functionsService.getFunctionByIdAndRevision(this.function.id, revisionId).subscribe((revision) => {
      this.revision = revision;
      this.selectedRevisionName = displayName;
      this.diffValue = { original: this.function.content, modified: this.revision.content };
    });
  }

  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    this.toggleFullscreenIcon = this.isFullscreen ? 'fullscreen-exit' : 'arrows-fullscreen';
    this.activeModal.update({ fullscreen: this.isFullscreen });
  }

  addMik() {
    const mik = this.newMik.trim();
    if (mik !== '') {
      this.function.miks.add(mik);
    }
    this.newMik = '';
  }

  deleteMik(item: string) {
    this.function.miks.delete(item);
  }
}
