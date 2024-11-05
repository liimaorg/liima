import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
//import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { DeploymentLog } from './deployment-log';

type Failed = 'failed';
@Component({
  selector: 'app-deployment-log-content',
  template: `
    <div class="m-2 h-100">
      @if (content !== null && content !== 'failed') {

      <div class="h-100">
        <!--<ngx-codemirror
          class="h-100"
          [(ngModel)]="content.content"
          [options]="{
            lineNumbers: false,
            theme: 'default',
            mode: 'simplemode',
            readOnly: true
          }"
        ></ngx-codemirror>-->
      </div>

      } @else { ... }
    </div>
  `,
  standalone: true,
  imports: [
    FormsModule, //CodemirrorModule
  ],
})
export class DeploymentLogContentComponent {
  @Input() content: DeploymentLog | Failed;
}
