import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DeploymentService } from 'src/app/deployment/deployment.service';
import { DeploymentsStoreService } from '../deployments-store.service';

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss'],
})
export class LogsComponent implements OnInit {
  deployment$;

  constructor(
    private deploymentService: DeploymentService,
    private deploymentStore: DeploymentsStoreService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const deploymentId: number = +params.get('deploymentId');
      this.deployment$ = this.deploymentStore.withid$(deploymentId);
      this.deployment$.subscribe((deployment) => {
        if (deployment === undefined) {
          this.deploymentService.get(deploymentId).subscribe((deployment) => {
            this.deploymentStore.deployments = [deployment];
          });
        }
      });
    });
  }
}
