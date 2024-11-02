import { Component, inject } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { HttpClient } from '@angular/common/http';
import { AsyncPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

type Config = { key: { value: string; env: string }; value: string; defaultValue: string };
type Version = { key: string; value: string };

@Component({
  selector: 'app-application-info',
  standalone: true,
  imports: [LoadingIndicatorComponent, AsyncPipe],
  templateUrl: './application-info.component.html',
})
export class ApplicationInfoComponent {
  private http = inject(HttpClient);
  appVersions$ = this.http.get<Version[]>('/AMW_rest/resources/settings/appInfo');
  appConfigs$ = this.http.get<Config[]>('/AMW_rest/resources/settings');
  isLoading$ = forkJoin([this.appVersions$, this.appConfigs$]).pipe(map(() => false));
}
