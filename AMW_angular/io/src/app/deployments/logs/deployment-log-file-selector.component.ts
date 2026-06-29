import { Component, EventEmitter, Output, input } from '@angular/core';
import { DeploymentLog } from './deployment-log';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';

type Failed = 'failed';
@Component({
  selector: 'app-deployment-logs-selector',
  template: `
    @if (logFiles() === 'failed') {
      <span>Unable to load log files</span>
    }
    @if (logFilesList(); as logFiles) {
      <div class="card-header">
        <div ngbDropdown class="d-inline-block">
          <button class="btn btn-outline-primary" id="filename-picker" ngbDropdownToggle>
            {{ selectedLogFile()?.filename }}
          </button>
          <div ngbDropdownMenu aria-labelledby="filename-picker">
            @for (logFile of logFiles; track logFile.id) {
              <button ngbDropdownItem (click)="selectFile(logFile)">
                {{ logFile.filename }}
              </button>
            }
          </div>
        </div>
      </div>
    }
  `,
  imports: [NgbDropdownMenu, NgbDropdownItem, NgbDropdownToggle, NgbDropdown],
})
export class DeploymentLogFileSelectorComponent {
  logFiles = input.required<DeploymentLog[] | Failed>();
  selected = input.required<DeploymentLog | Failed | null>();

  @Output() fileSelected = new EventEmitter<DeploymentLog>();

  logFilesList(): DeploymentLog[] | null {
    const logFiles = this.logFiles();
    return logFiles !== 'failed' ? logFiles : null;
  }

  selectedLogFile(): DeploymentLog | null {
    const selected = this.selected();
    return selected !== null && selected !== 'failed' ? selected : null;
  }

  selectFile(deploymentLog: DeploymentLog) {
    this.fileSelected.emit(deploymentLog);
  }
}
