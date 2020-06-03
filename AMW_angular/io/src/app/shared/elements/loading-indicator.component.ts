import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-loading-indicator',
  template: `
    <div *ngIf="isLoading" class="loading">
      <div class="loading-bg"></div>
    </div>
  `,
  styleUrls: ['./loading-indicator.component.scss']
})
export class LoadingIndicatorComponent {
  @Input()
  isLoading: boolean;

  constructor() {}
}
