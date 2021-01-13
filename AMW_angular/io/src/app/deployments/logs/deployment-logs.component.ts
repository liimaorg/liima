import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { merge, Observable, of, Subject } from 'rxjs';
import { catchError, map, shareReplay, startWith, switchMap } from 'rxjs/operators';
import { Deployment } from 'src/app/deployment/deployment';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { NavigationStoreService } from 'src/app/navigation/navigation-store.service';
import { DeploymentLog, DeploymentLogContent } from './deployment-log';
import { DeploymentLogsService } from './deployment-logs.service';

declare var CodeMirror: any;

type Failed = 'failed';

function failed(): Observable<Failed> {
  return of('failed');
}

@Component({
  selector: 'app-logs',
  styleUrls: ['./deployment-logs.component.scss'],
  templateUrl: './deployment-logs.component.html',
  encapsulation: ViewEncapsulation.None,
})
export class DeploymentLogsComponent implements OnInit {
  constructor(
    private deploymentLogsService: DeploymentLogsService,
    private deploymentService: DeploymentService,
    private route: ActivatedRoute,
    private navigationStore: NavigationStoreService
  ) {
    this.pagetitle$.subscribe((title) => this.navigationStore.setPageTitle(title));
  }

  deploymentId$: Observable<number> = this.route.paramMap.pipe(map((params) => +params.get('deploymentId')));

  deployment$: Observable<Deployment | Failed> = this.deploymentId$.pipe(
    switchMap(this.loadDeployment.bind(this)),
    shareReplay(1)
  );

  pagetitle$: Observable<string> = this.deployment$.pipe(
    map((deployment) =>
      deployment === 'failed'
        ? ``
        : `Log file for ${deployment.id} (${deployment.appServerName}
          ${deployment.releaseName})`
    )
  );

  deploymentLogMetaData$: Observable<DeploymentLog[]> = this.deployment$.pipe(
    switchMap(this.loadDeploymentLogs.bind(this)),
    shareReplay(1)
  );

  selectDeploymentLog$: Subject<DeploymentLog> = new Subject();

  currentDeploymentLog$: Observable<DeploymentLog> = merge(
    this.deploymentLogMetaData$.pipe(map((logs) => logs[0])),
    this.selectDeploymentLog$
  );

  currentDeploymentLogContent$: Observable<DeploymentLogContent> = this.currentDeploymentLog$.pipe(
    switchMap(this.loadDeploymentLogContent.bind(this))
  );

  ngOnInit(): void {
    CodeMirror.defineSimpleMode('simplemode', {
      start: [
        {
          regex: /^.*\b(error|failure|failed|fatal|not found)\b.*$/i,
          token: 'error',
        },
        {
          regex: /^.*\b(warn.*)\b.*$/i,
          token: 'warning',
        },
      ],
    });
  }

  selectFile(filename: DeploymentLog) {
    this.selectDeploymentLog$.next(filename);
  }

  loadDeployment(id) {
    return this.deploymentService.get(id).pipe(catchError((error) => failed()));
  }

  loadDeploymentLogs(deployment: Deployment | Failed) {
    return deployment === 'failed' || deployment === undefined
      ? of([])
      : this.deploymentLogsService.getLogFileMetaData(deployment.id).pipe(catchError((error) => failed()));
  }

  loadDeploymentLogContent(deploymentLog: DeploymentLog | Failed) {
    return deploymentLog === 'failed' || deploymentLog === undefined
      ? of('')
      : this.deploymentLogsService.getLogFileContent(deploymentLog).pipe(
          map((content) => {
            return { content };
          }),
          catchError((error) => failed())
        );
  }
}
