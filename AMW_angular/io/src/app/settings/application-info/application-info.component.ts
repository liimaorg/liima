import { Component, inject } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { HttpClient } from '@angular/common/http';
import { AsyncPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { SettingService } from '../../setting/setting.service';

@Component({
  selector: 'app-application-info',
  standalone: true,
  imports: [LoadingIndicatorComponent, AsyncPipe],
  templateUrl: './application-info.component.html',
})
export class ApplicationInfoComponent {
  private http = inject(HttpClient);
  private settingService = inject(SettingService);
  appVersions$ = this.settingService.getAppInformation();
  appConfigs$ = this.settingService.getAllAppSettings();

  isLoading$ = forkJoin([this.appVersions$, this.appConfigs$]).pipe(map(() => false));
}
