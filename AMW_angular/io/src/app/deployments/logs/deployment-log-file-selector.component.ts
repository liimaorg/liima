import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DeploymentLog } from './deployment-log';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';

type Failed = 'failed';
@Component({
  selector: 'app-deployment-logs-selector',
  standalone: true,
  template: `
    @if(logFiles === 'failed') {
    <span>Unable to load log files</span>

    } @if(logFiles !== 'failed' && selected !== 'failed') {
    <div class="card-header">
      <div ngbDropdown class="d-inline-block">
        <button class="btn btn-outline-primary" id="filename-picker" ngbDropdownToggle>
          {{ selected?.filename }}
        </button>
        <div ngbDropdownMenu aria-labelledby="filename-picker">
          @for (logFile of logFiles; track logFile.id) {
          <button ngbDropdownItem (click)="selectFile(logFile)">
            {{ logFile.filename }}</button
          >}
        </div>
      </div>
    </div>
    }
  `,
  imports: [NgbDropdownMenu, NgbDropdownItem, NgbDropdownToggle, NgbDropdown],
})
export class DeploymentLogFileSelectorComponent {
  @Input() logFiles: DeploymentLog[] | Failed;
  @Input() selected: DeploymentLog | Failed;

  @Output() fileSelected = new EventEmitter<DeploymentLog>();

  selectFile(deploymentLog: DeploymentLog) {
    this.fileSelected.emit(deploymentLog);
  }
}
