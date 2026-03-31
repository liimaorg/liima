import { Component, computed, effect, inject, input, signal, TemplateRef, ViewChild } from '@angular/core';
import { ResourceApplicationsService } from '../../services/resource-applications.service';
import { ApplicationRelation } from '../../models/application-relation';
import { AuthService } from '../../../auth/auth.service';
import { RouterLink } from '@angular/router';
import { TileComponent } from '../../../shared/tile/tile.component';
import { LoadingIndicatorComponent } from '../../../shared/elements/loading-indicator.component';
import { ButtonComponent } from '../../../shared/button/button.component';
import { Resource } from '../../models/resource';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalHeaderComponent } from '../../../shared/modal-header/modal-header.component';
import { ToastService } from '../../../shared/elements/toast/toast.service';
import { ResourceService } from '../../services/resource.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-resource-applications',
  standalone: true,
  imports: [RouterLink, TileComponent, LoadingIndicatorComponent, ButtonComponent, ModalHeaderComponent, FormsModule],
  templateUrl: './resource-applications.component.html',
  styleUrl: './resource-applications.component.scss',
})
export class ResourceApplicationsComponent {
  private resourceApplicationsService = inject(ResourceApplicationsService);
  private authService = inject(AuthService);
  private modalService = inject(NgbModal);
  private toastService = inject(ToastService);
  private resourceService = inject(ResourceService);

  resource = input.required<Resource>();
  contextId = input.required<number>();

  applications = signal<ApplicationRelation[]>([]);
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);
  applicationToRemove = signal<ApplicationRelation | null>(null);
  availableApplications = signal<Resource[]>([]);
  selectedApplicationGroupId = signal<number | null>(null);

  @ViewChild('removeApplicationConfirmation') removeApplicationConfirmation!: TemplateRef<void>;
  @ViewChild('addApplicationModal') addApplicationModal!: TemplateRef<void>;

  permissions = computed(() => {
    if (this.authService.restrictions().length > 0 && this.resource()) {
      return {
        canListRelations: this.authService.hasPermission('RESOURCE', 'READ'),
        canAdd:
          this.contextId() === 1 &&
          this.authService.hasPermission('RESOURCE', 'UPDATE', this.resource().type, this.resource().resourceGroupId),
        canRemove:
          this.contextId() === 1 &&
          this.authService.hasPermission('RESOURCE', 'UPDATE', this.resource().type, this.resource().resourceGroupId),
      };
    }
    return { canListRelations: false, canAdd: false, canRemove: false };
  });

  constructor() {
    effect(() => {
      const id = this.resource().id;
      if (!id) return;
      if (this.resource().type === '"APPLICATIONSERVER"') return;
      if (!this.permissions().canListRelations) return;

      this.loadApplications(id);
    });
  }

  private loadApplications(resourceId: number): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.resourceApplicationsService.getApplicationsForResource(resourceId).subscribe({
      next: (applications) => {
        this.applications.set(applications);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load applications:', err);
        this.error.set('Failed to load applications');
        this.isLoading.set(false);
      },
    });
  }

  showRemoveConfirmation(application: ApplicationRelation): void {
    this.applicationToRemove.set(application);
    this.modalService.open(this.removeApplicationConfirmation).result.then(
      () => {
        this.removeApplication();
      },
      () => {
        this.applicationToRemove.set(null);
      },
    );
  }

  private removeApplication(): void {
    const app = this.applicationToRemove();
    if (!app) return;

    this.isLoading.set(true);
    this.resourceApplicationsService.removeApplication(this.resource().id, app.id).subscribe({
      next: () => {
        this.toastService.success('Application removed successfully.');
        this.applicationToRemove.set(null);
        this.loadApplications(this.resource().id);
      },
      error: (err) => {
        console.error('Failed to remove application:', err);
        this.toastService.error('Failed to remove application.');
        this.isLoading.set(false);
        this.applicationToRemove.set(null);
      },
    });
  }

  showAddApplicationModal(): void {
    this.loadAvailableApplications();
    this.selectedApplicationGroupId.set(null);
    this.modalService.open(this.addApplicationModal, { size: 'lg' });
  }

  private loadAvailableApplications(): void {
    this.resourceService.getGroupsForTypeName('APPLICATION').subscribe({
      next: (apps) => {
        // Filter out already added applications
        const currentAppIds = new Set(this.applications().map((a) => a.slaveId));
        const available = apps.filter((app) => !currentAppIds.has(app.id));
        this.availableApplications.set(available);
      },
      error: (err) => {
        console.error('Failed to load available applications:', err);
        this.toastService.error('Failed to load available applications.');
      },
    });
  }

  addApplication(): void {
    const appGroupId = this.selectedApplicationGroupId();
    if (!appGroupId) {
      this.toastService.error('Please select an application.');
      return;
    }

    this.isLoading.set(true);
    this.resourceApplicationsService.addApplication(this.resource().id, appGroupId).subscribe({
      next: () => {
        this.toastService.success('Application added successfully.');
        this.modalService.dismissAll();
        this.loadApplications(this.resource().id);
      },
      error: (err) => {
        console.error('Failed to add application:', err);
        this.toastService.error('Failed to add application.');
        this.isLoading.set(false);
      },
    });
  }
}
