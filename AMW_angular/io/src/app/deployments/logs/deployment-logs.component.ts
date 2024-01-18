import { Location, NgIf, NgFor, AsyncPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest, merge, Observable, of, Subject } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, switchMap, takeUntil } from 'rxjs/operators';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { DeploymentLog, DeploymentLogContent } from './deployment-log';
import { DeploymentLogsService } from './deployment-logs.service';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { FormsModule } from '@angular/forms';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import {
  NgbDropdown,
  NgbDropdownToggle,
  NgbDropdownMenu,
  NgbDropdownButtonItem,
  NgbDropdownItem,
} from '@ng-bootstrap/ng-bootstrap';
import { NotificationComponent } from '../../shared/elements/notification/notification.component';
import { PageComponent } from '../../layout/page/page.component';

declare let CodeMirror: any;

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
    PageComponent,
  ],
})
export class DeploymentLogsComponent implements OnInit, OnDestroy {
  constructor(
    private deploymentLogsService: DeploymentLogsService,
    private deploymentService: DeploymentService,
    private route: ActivatedRoute,
    private location: Location,
  ) {}

  /**
   * the deployment id taken from the route param
   */
  deploymentId$: Observable<number> = this.route.paramMap.pipe(
    map((params) => +params.get('deploymentId')),
    distinctUntilChanged(),
  );

  /**
   * the file name from the path param - may be empty
   */
  fileName$: Observable<string> = this.route.paramMap.pipe(
    map((params) => params.get('fileName')),
    distinctUntilChanged(),
  );

  selectedDeploymentLog$: Subject<DeploymentLog> = new Subject();

  availableLogFiles$: Observable<DeploymentLog[]> = this.deploymentId$.pipe(
    switchMap(this.loadAvailableDeploymentLogFileNames.bind(this)),
    shareReplay(1),
  );

  currentDeploymentLog$: Observable<DeploymentLog> = merge(
    combineLatest([this.fileName$, this.availableLogFiles$]).pipe(
      map(([filename, availableLogFiles]) =>
        !filename ? availableLogFiles[0] : availableLogFiles.find((m) => m.filename === filename),
      ),
    ),
    this.selectedDeploymentLog$,
  ).pipe(distinctUntilChanged());

  selectedDeploymentLogContent$: Observable<DeploymentLogContent> = this.currentDeploymentLog$.pipe(
    switchMap(this.loadDeploymentLogContent.bind(this)),
  );

  private destroy$ = new Subject<void>();
  ngOnInit(): void {
    this.currentDeploymentLog$
      .pipe(takeUntil(this.destroy$))
      .subscribe((current) =>
        this.location.replaceState(`/deployments/${current.deploymentId}/logs/${current.filename}`),
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
    this.selectedDeploymentLog$.next(deploymentLogMetaData);
  }

  loadAvailableDeploymentLogFileNames(deploymentId: number) {
    return this.deploymentLogsService.getLogFileMetaData(deploymentId).pipe(catchError(() => failed()));
  }

  loadDeploymentLogContent(deploymentLog: DeploymentLog | Failed) {
    return deploymentLog === 'failed' || deploymentLog === undefined
      ? of('')
      : this.deploymentLogsService.getLogFileContent(deploymentLog).pipe(catchError(() => failed()));
  }
}
