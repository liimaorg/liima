import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ResourceFunction } from 'src/app/resources/models/resource-function';
import { ResourceFunctionsService } from 'src/app/resources/services/resource-functions.service';
import { ButtonComponent } from 'src/app/shared/button/button.component';
import { CodeEditorComponent } from 'src/app/shared/codemirror/code-editor.component';
import { DiffEditorComponent } from 'src/app/shared/codemirror/diff-editor.component';
import { FullscreenToggleComponent } from 'src/app/shared/fullscreen-toggle/fullscreen-toggle.component';
import { IconComponent } from 'src/app/shared/icon/icon.component';
import { ModalHeaderComponent } from 'src/app/shared/modal-header/modal-header.component';
import { RevisionInformation } from 'src/app/shared/model/revisionInformation';
import { RevisionCompareComponent } from 'src/app/shared/revision-compare/revision-compare.component';

@Component({
  selector: 'app-resource-function-edit',
  templateUrl: './resource-function-edit.component.html',
  styleUrl: './resource-function-edit.component.scss',
  standalone: true,
  imports: [
    CodeEditorComponent,
    DiffEditorComponent,
    FormsModule,
    IconComponent,
    NgbDropdownModule,
    ModalHeaderComponent,
    ButtonComponent,
    ModalHeaderComponent,
    IconComponent,
    RevisionCompareComponent,
    FullscreenToggleComponent,
  ],
})
export class ResourceFunctionEditComponent implements OnInit {
  activeModal = inject(NgbActiveModal);

  @Input() function: ResourceFunction;
  @Input() canEdit: boolean;
  @Input() isOverwrite: boolean;

  @Output() saveFunction: EventEmitter<ResourceFunction> = new EventEmitter<ResourceFunction>();

  private functionsService = inject(ResourceFunctionsService);
  public revisions: RevisionInformation[] = [];
  public revision: ResourceFunction;
  public selectedRevisionName: string;
  public newMik: string = '';
  public diffValue = {
    original: '',
    modified: '',
  };

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
