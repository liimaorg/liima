import { Component, EventEmitter, inject, Input, Output, signal, WritableSignal } from '@angular/core';
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
export class ResourceFunctionEditComponent {
  activeModal = inject(NgbActiveModal);

  private _function!: ResourceFunction;
  @Input() set function(value: ResourceFunction) {
    this._function = value;
    if (value && value.id) {
      this.loadRevisions(value.id);
    }
  }
  get function(): ResourceFunction {
    return this._function;
  }

  private readonly canEditSignal = signal(false);
  private readonly isOverwriteSignal = signal(false);

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set canEdit(value: boolean) {
    this.canEditSignal.set(value);
  }

  get canEdit(): boolean {
    return this.canEditSignal();
  }

  @Input({ required: true })
  set isOverwrite(value: boolean) {
    this.isOverwriteSignal.set(value);
  }

  get isOverwrite(): boolean {
    return this.isOverwriteSignal();
  }

  @Output() saveFunction: EventEmitter<ResourceFunction> = new EventEmitter<ResourceFunction>();

  public wrapLinesEnabled = false;
  private functionsService = inject(ResourceFunctionsService);
  public revisions: WritableSignal<RevisionInformation[]> = signal([]);
  public revision: WritableSignal<ResourceFunction | null> = signal(null);
  public selectedRevisionName: string | null = null;
  public newMik: string = '';
  public diffValue = {
    original: '',
    modified: '',
  };

  getTitle(): string {
    return (this.function.id ? (this.isOverwrite ? 'Overwrite' : 'Edit') : 'Add') + ' function';
  }

  cancel() {
    this.activeModal.close();
  }

  save() {
    if (this.revision()) this.function.content = this.diffValue.original;
    if (this.newMik !== '') this.addMik();
    this.saveFunction.emit(this.function);
    this.activeModal.close();
  }

  loadRevisions(functionId: number): void {
    this.functionsService.getFunctionRevisions(functionId).subscribe((revisions) => {
      this.revisions.set(revisions);
    });
  }

  selectRevision(revisionId: number | null, displayName: string | null): void {
    if (revisionId && displayName) {
      this.functionsService.getFunctionByIdAndRevision(this.function.id!, revisionId).subscribe((revision) => {
        this.revision.set(revision);
        this.selectedRevisionName = displayName;
        this.diffValue = { original: this.function.content, modified: this.revision()!.content };
      });
    } else {
      //reset selected revision
      this.revision.set(null);
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
