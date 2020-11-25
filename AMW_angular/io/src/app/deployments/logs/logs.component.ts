import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { DeploymentsStoreService } from '../deployments-store.service';
import { DeploymentLogStoreService } from './deployment-log-store.service';

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss'],
})
export class LogsComponent implements OnInit {
  deployment$;
  deploymentLogs$;

  constructor(
    private deploymentService: DeploymentService,
    private deploymentStore: DeploymentsStoreService,
    public deploymentLogStore: DeploymentLogStoreService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const deploymentId: number = +params.get('deploymentId');

      this.deploymentLogs$ = this.deploymentLogStore.fetch(deploymentId);
      this.deployment$ = this.deploymentStore.withid$(deploymentId);

      this.deployment$.subscribe((deployment) => {
        if (deployment === undefined) {
          this.deploymentService.get(deploymentId).subscribe((d) => {
            this.deploymentStore.deployments = [d];
          });
        }
      });
    });
  }
}
