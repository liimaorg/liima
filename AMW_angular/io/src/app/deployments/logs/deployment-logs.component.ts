import { Location, NgIf, NgFor, AsyncPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, merge, Observable, of, Subject } from 'rxjs';
import { catchError, distinctUntilChanged, map, shareReplay, switchMap, takeUntil } from 'rxjs/operators';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { DeploymentLog } from './deployment-log';
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
import { PageComponent } from '../../layout/page/page.component';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { NotificationComponent } from '../../shared/elements/notification/notification.component';
import { Deployment } from '../../deployment/deployment';
import { DeploymentLogContentComponent } from './deployment-log-content.component';
import { DeploymentLogFileSelectorComponent } from './deployment-log-file-selector.component';

declare let CodeMirror: any;

type Failed = 'failed';

const FAIL: Failed = 'failed';

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
    NotificationComponent,
    DeploymentLogContentComponent,
    DeploymentLogFileSelectorComponent,
  ],
})
export class DeploymentLogsComponent implements OnInit, OnDestroy {
  constructor(
    private deploymentService: DeploymentService,
    private deploymentLogsService: DeploymentLogsService,
    private route: ActivatedRoute,
    private location: Location,
    private toastService: ToastService,
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

  deployment$: Observable<Deployment | Failed> = this.deploymentId$.pipe(
    switchMap(this.loadDeployment.bind(this)),
    shareReplay(1),
  );

  selectedDeploymentLog$: Subject<DeploymentLog> = new Subject();

  availableLogFiles$: Observable<DeploymentLog[] | Failed> = this.deployment$.pipe(
    switchMap(this.loadAvailableDeploymentLogFileNames.bind(this)),
    shareReplay(1),
  );

  currentDeploymentLog$: Observable<DeploymentLog | Failed> = merge(
    combineLatest([this.fileName$, this.availableLogFiles$]).pipe(
      map(([filename, availableLogFiles]) => {
        if (availableLogFiles === FAIL) {
          return FAIL;
        }
        return !filename ? availableLogFiles[0] : availableLogFiles.find((m) => m.filename === filename);
      }),
    ),
    this.selectedDeploymentLog$,
  ).pipe(distinctUntilChanged());

  selectedDeploymentLogContent$: Observable<DeploymentLog> = this.currentDeploymentLog$.pipe(
    switchMap(this.loadDeploymentLogContent.bind(this)),
  );

  private error$ = new BehaviorSubject<string>(null);

  private destroy$ = new Subject<void>();
  ngOnInit(): void {
    this.currentDeploymentLog$.pipe(takeUntil(this.destroy$)).subscribe((current: DeploymentLog | Failed) => {
      if (current === FAIL) {
        return;
      }
      this.location.replaceState(`/deployments/${current.id}/logs/${current.filename}`);
    });
    this.error$.pipe(takeUntil(this.destroy$)).subscribe((msg) => {
      if (msg !== null) this.toastService.error(msg);
    });

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

  selectFile(deploymentLog: DeploymentLog): void {
    this.selectedDeploymentLog$.next(deploymentLog);
  }

  loadDeployment(deploymentId) {
    return this.deploymentService.get(deploymentId).pipe(catchError(this.fail()));
  }

  loadAvailableDeploymentLogFileNames(deployment: Deployment | Failed): Observable<DeploymentLog[] | Failed> {
    return deployment === 'failed' || deployment === undefined
      ? of([])
      : this.deploymentLogsService.getLogFileMetaData(deployment.id).pipe(catchError(this.fail()));
  }

  loadDeploymentLogContent(deploymentLog: DeploymentLog | Failed): Observable<string | DeploymentLog> {
    return deploymentLog === 'failed' || deploymentLog === undefined
      ? of('')
      : this.deploymentLogsService.getLogFileContent(deploymentLog).pipe(catchError(this.fail()));
  }

  private fail() {
    return (msg) => {
      this.error$.next(msg);
      return failed();
    };
  }

  protected readonly FAIL = FAIL;
}
