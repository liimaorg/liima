import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { DeploymentLog, filenamePredicate, toFileName } from './deployment-log';
import { DeploymentLogService } from './deployment-log.service';

@Injectable({
  providedIn: 'root',
})
export class DeploymentLogStoreService {
  constructor(private deploymentLogService: DeploymentLogService) {}

  private readonly _deploymentLogs = new BehaviorSubject<DeploymentLog[]>([]);

  readonly deploymentLogs$ = this._deploymentLogs.asObservable();

  get deploymentLogs(): DeploymentLog[] {
    return this._deploymentLogs.getValue();
  }

  set deploymentLogs(val: DeploymentLog[]) {
    this._deploymentLogs.next(val);
  }

  readonly filenames$: Observable<string[]> = this.deploymentLogs$.pipe(
    map((deploymentLogs) => deploymentLogs.map(toFileName))
  );

  readonly withName$ = (filename: string): Observable<DeploymentLog> =>
    this.deploymentLogs$.pipe(
      map((deploymentLogs) => deploymentLogs.find(filenamePredicate(filename)))
    );

  async fetch(deploymentId: number) {
    this.deploymentLogs = await this.deploymentLogService
      .get(deploymentId)
      .toPromise();
  }
}
