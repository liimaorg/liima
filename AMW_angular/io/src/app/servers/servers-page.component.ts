import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-servers-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
  template: ` <h1 data-cy="page-title">Servers</h1>`,
})
export class ServersPageComponent {}
