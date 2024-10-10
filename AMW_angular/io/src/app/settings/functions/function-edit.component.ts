import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Function } from './function';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { FunctionsService } from './functions.service';

@Component({
  selector: 'amw-function-edit',
  templateUrl: './function-edit.component.html',
  styleUrl: './function-edit.component.scss',
  standalone: true,
  imports: [FormsModule, CodemirrorModule],
})
export class FunctionEditComponent {
  @Input() function: Function;
  @Input() canManage: boolean;
  @Output() saveFunction: EventEmitter<Function> = new EventEmitter<Function>();

  private functionsService = inject(FunctionsService);

  constructor(public activeModal: NgbActiveModal) {}

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
}
