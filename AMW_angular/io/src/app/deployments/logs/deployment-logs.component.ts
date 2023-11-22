import { Location, NgIf, NgFor, AsyncPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest, merge, Observable, of, Subject } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, switchMap, takeUntil } from 'rxjs/operators';
import { Deployment } from 'src/app/deployment/deployment';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { NavigationStoreService } from 'src/app/navigation/navigation-store.service';
import { DeploymentLog, DeploymentLogContent } from './deployment-log';
import { DeploymentLogsService } from './deployment-logs.service';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { FormsModule } from '@angular/forms';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { NgbDropdown, NgbDropdownToggle, NgbDropdownMenu, NgbDropdownButtonItem, NgbDropdownItem } from '@ng-bootstrap/ng-bootstrap';
import { NotificationComponent } from '../../shared/elements/notification/notification.component';

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
    standalone: true,
    imports: [
        NgIf,
        NotificationComponent,
        NgbDropdown,
        NgbDropdownToggle,
        NgbDropdownMenu,
        NgFor,
        NgbDropdownButtonItem,
        NgbDropdownItem,
        CodemirrorModule,
        FormsModule,
        LoadingIndicatorComponent,
        AsyncPipe,
    ],
})
export class DeploymentLogsComponent implements OnInit, OnDestroy {
  constructor(
    private deploymentLogsService: DeploymentLogsService,
    private deploymentService: DeploymentService,
    private route: ActivatedRoute,
    private location: Location,
    private navigationStore: NavigationStoreService
  ) {
    this.pagetitle$.subscribe((title) => this.navigationStore.setPageTitle(title));
  }

  selectDeploymentLog$: Subject<DeploymentLog> = new Subject();

  deploymentId$: Observable<number> = this.route.paramMap.pipe(
    map((params) => +params.get('deploymentId')),
    distinctUntilChanged()
  );

  fileName$: Observable<string> = this.route.paramMap.pipe(
    map((params) => params.get('fileName')),
    distinctUntilChanged()
  );

  deployment$: Observable<Deployment | Failed> = this.deploymentId$.pipe(
    switchMap(this.loadDeployment.bind(this)),
    shareReplay(1)
  );

  deploymentLogMetaData$: Observable<DeploymentLog[]> = this.deployment$.pipe(
    switchMap(this.loadDeploymentLogs.bind(this)),
    shareReplay(1)
  );

  currentDeploymentLog$: Observable<DeploymentLog> = merge(
    combineLatest([this.fileName$, this.deploymentLogMetaData$]).pipe(
      map(([filename, meta]) => (!filename ? meta[0] : meta.find((m) => m.filename === filename)))
    ),
    this.selectDeploymentLog$
  ).pipe(distinctUntilChanged());

  currentDeploymentLogContent$: Observable<DeploymentLogContent> = this.currentDeploymentLog$.pipe(
    switchMap(this.loadDeploymentLogContent.bind(this))
  );

  pagetitle$: Observable<string> = this.deployment$.pipe(
    map((deployment) =>
      deployment === 'failed'
        ? ``
        : `Log file for ${deployment.id} (${deployment.appServerName}
          ${deployment.releaseName})`
    )
  );

  private destroy$ = new Subject<void>();
  ngOnInit(): void {
    this.currentDeploymentLog$
      .pipe(takeUntil(this.destroy$))
      .subscribe((current) =>
        this.location.replaceState(`/deployments/${current.deploymentId}/logs/${current.filename}`)
      );
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

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  selectFile(deploymentLogMetaData: DeploymentLog) {
    this.selectDeploymentLog$.next(deploymentLogMetaData);
  }

  loadDeployment(deploymentId) {
    return this.deploymentService.get(deploymentId).pipe(catchError(() => failed()));
  }

  loadDeploymentLogs(deployment: Deployment | Failed) {
    return deployment === 'failed' || deployment === undefined
      ? of([])
      : this.deploymentLogsService.getLogFileMetaData(deployment.id).pipe(catchError(() => failed()));
  }

  loadDeploymentLogContent(deploymentLog: DeploymentLog | Failed) {
    return deploymentLog === 'failed' || deploymentLog === undefined
      ? of('')
      : this.deploymentLogsService.getLogFileContent(deploymentLog).pipe(
          map((content) => {
            return { content };
          }),
          catchError(() => failed())
        );
  }
}
