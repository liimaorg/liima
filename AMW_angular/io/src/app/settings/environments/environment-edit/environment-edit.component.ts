import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-environment-edit',
  standalone: true,
  imports: [],
  templateUrl: './environment-edit.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentEditComponent {}
