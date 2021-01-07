import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { merge, Observable, of, Subject } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';
import { Deployment } from 'src/app/deployment/deployment';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { NavigationStoreService } from 'src/app/navigation/navigation-store.service';
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
    private route: ActivatedRoute,
    private navigationStore: NavigationStoreService
  ) {  }


  deploymentId$: Observable<number> = this.route.paramMap.pipe(
    map((params) => +params.get('deploymentId'))
  );

  pagetitle$: Observable<string> = this.deploymentId$.pipe(
    map((id) => `Log file for ${id}`)
  )

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
      start: [
        { regex: /^.*\b(error|failure|failed|fatal)\b.*$/i, token: 'error' },
      ],
    });
    this.pagetitle$.subscribe(title => this.navigationStore.setPageTitle(title));
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
