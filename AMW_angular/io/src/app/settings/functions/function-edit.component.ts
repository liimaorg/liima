import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { AppFunction } from './appFunction';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { FunctionsService } from './functions.service';
import {RevisionInformation} from "./revisionInformation";
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'amw-function-edit',
  templateUrl: './function-edit.component.html',
  standalone: true,
  imports: [FormsModule, CodemirrorModule, CommonModule, NgbDropdownModule],
})
export class FunctionEditComponent {
  @Input() function: AppFunction;
  @Input() canManage: boolean;
  @Output() saveFunction: EventEmitter<AppFunction> = new EventEmitter<AppFunction>();

  private functionsService = inject(FunctionsService);
  public revisions: RevisionInformation[]= [];

  constructor(public activeModal: NgbActiveModal) {}

  ngOnInit(): void {
    this.loadRevisions(this.function.id);
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
    this.functionsService.getFunctionRevisions(functionId).subscribe(revisions => {
      console.log('Revisions received from API:', revisions);  // Debugging the data
      this.revisions = revisions;  // Ensure the revisions are set correctly
      console.log('Revisions in component:', this.revisions);  // Log after setting to ensure it's correct
    });
  }

}
