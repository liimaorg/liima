import { Component, OnInit} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AppState } from '../app.service';
import * as _ from 'lodash';

@Component({
  selector: 'amw-auditview',
  templateUrl: './auditview.component.html'
})

export class AuditviewComponent implements OnInit {

  constructor(public appState: AppState,
              private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    this.appState.set('navShow', false);
    this.appState.set('pageTitle', 'Audit View');
  }



}


