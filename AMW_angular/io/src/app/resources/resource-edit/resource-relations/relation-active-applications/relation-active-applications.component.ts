import { Component, computed, effect, inject, input, output } from '@angular/core';
import { ResourceActivationService, ResourceActivation } from '../../../services/resource-activation.service';
import { FormsModule } from '@angular/forms';
import { LoadingIndicatorComponent } from '../../../../shared/elements/loading-indicator.component';

@Component({
  selector: 'app-relation-active-applications',
  standalone: true,
  imports: [FormsModule, LoadingIndicatorComponent],
  templateUrl: './relation-active-applications.component.html',
})
export class RelationActiveApplicationsComponent {
  private resourceActivationService = inject(ResourceActivationService);

  resourceId = input.required<number>();
  relationId = input.required<number>();
  contextId = input.required<number>();
  isApplicationServerToNode = input.required<boolean>();
  canEdit = input.required<boolean>();
  activeApplicationIds = input.required<number[]>();

  activationChange = output<number[]>();

  constructor() {
    effect(() => {
      const resourceId = this.resourceId();
      const relationId = this.relationId();
      const contextId = this.contextId();
      const isAppServerToNode = this.isApplicationServerToNode();

      if (resourceId && relationId && contextId && isAppServerToNode) {
        this.resourceActivationService.setRelationParams(resourceId, relationId, contextId);
      }
    });
  }

  activations = computed(() => this.resourceActivationService.activations());
  isLoading = this.resourceActivationService.isLoading;
  hasApplications = computed(() => this.activations().length > 0);

  onCheckboxChange(applicationId: number, checked: boolean) {
    const current = this.activeApplicationIds();
    let updated: number[];

    if (checked) {
      updated = [...current, applicationId];
    } else {
      updated = current.filter((id) => id !== applicationId);
    }

    this.activationChange.emit(updated);
  }

  isApplicationActive(applicationId: number): boolean {
    return this.activeApplicationIds().includes(applicationId);
  }
}
