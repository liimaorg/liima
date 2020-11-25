import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, first, map, take } from 'rxjs/operators';
import { Deployment } from '../deployment/deployment';

@Injectable({
  providedIn: 'root',
})
export class DeploymentsStoreService {
  constructor() {}

  private readonly _deployments = new BehaviorSubject<Deployment[]>([]);

  readonly deployments$ = this._deployments.asObservable();

  get deployments(): Deployment[] {
    return this._deployments.getValue();
  }

  set deployments(val: Deployment[]) {
    this._deployments.next(val);
  }

  readonly withid$ = (id: number): Observable<Deployment> =>
    this.deployments$.pipe(
      map((deployments) =>
        deployments.find((deployment) => deployment.id === id)
      )
    );
}
