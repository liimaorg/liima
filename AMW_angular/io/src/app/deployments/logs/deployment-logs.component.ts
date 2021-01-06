import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { merge, Observable, of, Subject } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import { Deployment } from 'src/app/deployment/deployment';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { DeploymentLog } from './deployment-log';
import { DeploymentLogsService } from './deployment-logs.service';

declare var CodeMirror: any;

type Failed = 'failed';

function failed(): Observable<Failed> {
  return of('failed');
}

@Component({
  selector: 'app-logs',
  templateUrl: './deployment-logs.component.html',
})
export class DeploymentLogsComponent implements OnInit {
  constructor(
    private deploymentLogsService: DeploymentLogsService,
    private deploymentService: DeploymentService,
    private route: ActivatedRoute
  ) {}

  deploymentId$: Observable<number> = this.route.paramMap.pipe(
    map((params) => +params.get('deploymentId'))
  );

  deployment$: Observable<Deployment | Failed> = this.deploymentId$.pipe(
    switchMap(this.loadDeployment.bind(this)),
    shareReplay(1)
  );

  deploymentLogs$: Observable<DeploymentLog[]> = this.deployment$.pipe(
    switchMap(this.loadDeploymentLogs.bind(this)),
    shareReplay(1)
  );

  selectDeploymentLog$: Subject<DeploymentLog> = new Subject();

  currentDeploymentLog$: Observable<DeploymentLog> = merge(
    this.deploymentLogs$.pipe(map((logs) => logs[0])),
    this.selectDeploymentLog$
  );

  ngOnInit(): void {
    CodeMirror.defineSimpleMode('simplemode', {
      start: [{ regex: /(error|failure|failed|fatal)\b/i, token: 'error' }],
    });
  }

  selectFile(filename: DeploymentLog) {
    this.selectDeploymentLog$.next(filename);
  }

  loadDeployment(id) {
    return this.deploymentService.get(id).pipe(catchError((error) => failed()));
  }

  loadDeploymentLogs(deployment) {
    return deployment === 'failed' || deployment === undefined
      ? of([])
      : this.deploymentLogsService.get(deployment.id);
  }
}
