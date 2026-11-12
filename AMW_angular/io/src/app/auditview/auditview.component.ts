import { ChangeDetectionStrategy, Component, effect, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { AuditviewService } from './auditview.service';
import { ResourceService } from '../resources/services/resource.service';
import { AuditLogEntry } from './auditview-entry';
import { AuditviewTableComponent } from './auditview-table/auditview-table.component';
import { NotificationComponent } from '../shared/elements/notification/notification.component';
import { DatePipe } from '@angular/common';
import { LoadingIndicatorComponent } from '../shared/elements/loading-indicator.component';
import { AuditviewTableService } from './auditview-table/auditview-table.service';
import { PageComponent } from '../layout/page/page.component';

@Component({
  selector: 'app-auditview',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './auditview.component.html',
  providers: [AuditviewService, AuditviewTableService, DatePipe],
  imports: [LoadingIndicatorComponent, NotificationComponent, AuditviewTableComponent, PageComponent],
})
export class AuditviewComponent implements OnInit {
  private auditViewService = inject(AuditviewService);
  private resourceService = inject(ResourceService);
  private activatedRoute = inject(ActivatedRoute);
  private title = inject(Title);

  name = signal<string | undefined>(undefined);
  auditLogEntries: AuditLogEntry[] = [];
  errorMessage: string | undefined;
  successMessage: string | undefined;
  resourceId: number | undefined;
  isLoading = signal(true);

  constructor() {
    effect(() => {
      const resourceName = this.name();
      this.title.setTitle(resourceName ? `Audit View for ${resourceName} - Liima` : 'Audit View - Liima');
    });
  }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((param: any) => {
      if (param['resourceId']) {
        try {
          this.resourceId = JSON.parse(param['resourceId']);
        } catch {
          this.errorMessage = 'Error parsing resourceId';
        }
      }
    });

    if (this.resourceId) {
      this.resourceService.getResourceName(this.resourceId).subscribe({
        next: (resource) => this.name.set(resource.name),
        error: (e) => (this.errorMessage = e),
      });
      this.auditViewService.getAuditLogForResource(this.resourceId).subscribe({
        next: (auditLogEntries) => (this.auditLogEntries = auditLogEntries),
        error: (e) => (this.errorMessage = e),
        complete: () => this.isLoading.set(false),
      });
    } else {
      this.errorMessage = 'Parameter resourceId must be set!';
      this.isLoading.set(false);
    }
  }
}
