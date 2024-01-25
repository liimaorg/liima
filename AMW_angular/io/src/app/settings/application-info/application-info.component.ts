import { Component } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { HttpClient } from '@angular/common/http';
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { BehaviorSubject, forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

type Config = { key: { value: string; env: string }; value: string; defaultValue: string };
type Version = { key: string; value: string };

@Component({
  selector: 'amw-application-info',
  standalone: true,
  imports: [LoadingIndicatorComponent, NgIf, NgFor, AsyncPipe],
  templateUrl: './application-info.component.html',
})
export class ApplicationInfoComponent {
  appVersions$: Observable<Version[]>;
  appConfigs$: Observable<Config[]>;
  isLoading$: Observable<boolean> = new BehaviorSubject<boolean>(true);

  constructor(private http: HttpClient) {
    this.appVersions$ = http.get<Version[]>('/AMW_rest/resources/settings/appInfo');
    this.appConfigs$ = http.get<Config[]>('/AMW_rest/resources/settings');
    this.isLoading$ = forkJoin([this.appVersions$, this.appConfigs$]).pipe(map(() => false));
  }
}
