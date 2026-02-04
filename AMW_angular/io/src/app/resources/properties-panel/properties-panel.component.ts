import { Component, input, output } from '@angular/core';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-properties-panel',
  standalone: true,
  imports: [ButtonComponent],
  templateUrl: './properties-panel.component.html',
  styleUrl: './properties-panel.component.scss',
})
export class PropertiesPanelComponent {
  canEdit = input<boolean>(false);
  isSaving = input<boolean>(false);
  hasChanges = input<boolean>(false);
  errorMessage = input<string | null>(null);
  successMessage = input<string | null>(null);

  cancelAction = output<void>();
  saveAction = output<void>();
}
