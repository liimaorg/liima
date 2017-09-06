import { Component, Input } from '@angular/core';
import { Deployment } from './deployment';

@Component({
  selector: 'amw-deployments-list',
  templateUrl: './deployments-list.component.html'
})

export class DeploymentsListComponent {

  @Input() deployments: Deployment[] = [];

}
