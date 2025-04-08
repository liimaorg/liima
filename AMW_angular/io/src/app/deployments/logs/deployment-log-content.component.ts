import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DeploymentLog } from './deployment-log';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';

type Failed = 'failed';
@Component({
    selector: 'app-deployment-log-content',
    template: `
    <div class="m-2 h-100">
      @if (content !== null && content !== 'failed') {

      <div class="editor border border-primary-subtle rounded">
        <app-code-editor
          [theme]="'light'"
          [setup]="'minimal'"
          [readonly]="true"
          [(ngModel)]="content.content"
        ></app-code-editor>
      </div>

      } @else { ... }
    </div>
  `,
    imports: [FormsModule, CodeEditorComponent]
})
export class DeploymentLogContentComponent {
  @Input() content: DeploymentLog | Failed;
}
