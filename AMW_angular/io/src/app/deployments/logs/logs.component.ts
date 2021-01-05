import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Deployment } from 'src/app/deployment/deployment';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { DeploymentsStoreService } from '../deployments-store.service';
import { DeploymentLog } from './deployment-log';
import { DeploymentLogStoreService } from './deployment-log-store.service';

declare var CodeMirror: any;
@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss'],
})
export class LogsComponent implements OnInit {
  deployment$: Observable<Deployment>;
  deploymentLogs$: Observable<DeploymentLog[]>;

  currentFilename = new BehaviorSubject<string>('');

  readonly deploymentLog$ = this.currentFilename.pipe(
    switchMap(this.deploymentLogStore.withName$)
  );

  constructor(
    private deploymentService: DeploymentService,
    private deploymentStore: DeploymentsStoreService,
    public deploymentLogStore: DeploymentLogStoreService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    CodeMirror.defineSimpleMode('simplemode', {
      start: [{ regex: /(error|failure|failed|fatal)\b/i, token: 'error' }],
    });

    this.route.paramMap.subscribe((params) => {
      const deploymentId: number = +params.get('deploymentId');

      this.deployment$ = this.deploymentStore.withid$(deploymentId);
      this.deploymentLogStore.fetch(deploymentId);
      this.deploymentLogStore.filenames$.subscribe((filenames) =>
        this.currentFilename.next(filenames[0])
      );

      this.deployment$.subscribe((deployment) => {
        if (deployment === undefined) {
          this.deploymentService.get(deploymentId).subscribe((d) => {
            this.deploymentStore.deployments = [d];
          });
        }
      });
    });
  }

  selectFile(filename: string) {
    this.currentFilename.next(filename);
  }
}
