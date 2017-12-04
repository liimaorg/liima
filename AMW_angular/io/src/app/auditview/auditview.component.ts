import { Component, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import { AuditviewService } from './auditview.service';
import { Auditviewentrytype } from './auditview-entry-type';

import * as _ from 'lodash';

@Component({
  selector: 'amw-auditview',
  templateUrl: './auditview.component.html'
})

export class AuditviewComponent implements OnInit {

  auditLogEntries: Auditviewentrytype[] = [];

  errorMessage: string;
  contextId: number;
  resourceId: number;


  constructor(public appState: AppState,
              private auditViewService: AuditviewService,
              private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.appState.set('navShow', false);
    this.appState.set('pageTitle', 'Audit View');

    this.activatedRoute.queryParams.subscribe(
      (param: any) => {
        if (param['contextId']) {
          try {
            this.contextId = JSON.parse(param['contextId']);
            console.log(this.contextId);
          } catch (e) {
            console.error(e);
            this.errorMessage = 'Error parsing contextId';
          }
        } else {
          this.contextId = 1; // TODO get global context from REST
        }
        if (param['resourceId']) {
          try {
            this.resourceId = JSON.parse(param['resourceId']);
            console.log(this.contextId);
          } catch (e) {
            console.error(e);
            this.errorMessage = 'Error parsing resourceId';
          }
        }
      });

    console.log(this.contextId);

    if (this.resourceId) {
      this.auditViewService.getAuditLogForResource(this.resourceId, this.contextId).subscribe(
        /* happy path */ (r) => this.auditLogEntries = r,
        /* error path */ (e) => this.errorMessage = e,
        /* onComplete */ () => {}
        );
    } else {
      console.error("Resource Id must be set")
    }

  }



}


