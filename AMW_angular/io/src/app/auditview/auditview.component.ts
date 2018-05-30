import { Component, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { AuditviewService } from './auditview.service';
import { ResourceService } from '../resource/resource.service';
import { Auditviewentrytype } from './auditview-entry-type';

@Component({
  selector: 'amw-auditview',
  templateUrl: './auditview.component.html'
})

export class AuditviewComponent implements OnInit {

  name: string;
  auditLogEntries: Auditviewentrytype[] = [];
  filterQuery: string = '';
  errorMessage: string;
  resourceId: number;
  isLoading: boolean = true;

  constructor(public appState: AppState,
              private auditViewService: AuditviewService,
              private resourceService: ResourceService,
              private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.appState.set('navShow', false);
    this.appState.set('pageTitle', 'Audit View');

    this.activatedRoute.queryParams.subscribe(
      (param: any) => {
        if (param['resourceId']) {
          try {
            this.resourceId = JSON.parse(param['resourceId']);
          } catch (e) {
            console.error(e);
            this.errorMessage = 'Error parsing resourceId';
          }
        }
      });

    if (this.resourceId) {
      this.resourceService.getResourceName(this.resourceId).subscribe(
        /* happy path */ (r) => this.name = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => {}
      );
      this.auditViewService.getAuditLogForResource(this.resourceId).subscribe(
        /* happy path */ (r) => this.auditLogEntries = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false
      );
    } else {
      console.error('Resource Id must be set');
    }
  }
}


