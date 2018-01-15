import { Component, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { AuditviewService } from './auditview.service';
import { ResourceService } from '../resource/resource.service';
import { Auditviewentrytype } from './auditview-entry-type';

import * as _ from 'lodash';


@Component({
  selector: 'amw-auditview',
  templateUrl: './auditview.component.html'
})

export class AuditviewComponent implements OnInit {

  name: string;
  entriesLoaded: boolean = false;
  auditLogEntries: Auditviewentrytype[] = [];
  filterQuery: string = "";

  errorMessage: string;
  resourceId: number;


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
      )
      this.auditViewService.getAuditLogForResource(this.resourceId).subscribe(
        /* happy path */ (r) => this.auditLogEntries = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => {this.entriesLoaded = true}
      );
    } else {
      console.error("Resource Id must be set")
    }
  }
}


