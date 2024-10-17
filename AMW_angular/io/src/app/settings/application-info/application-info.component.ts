import { Component, computed, inject } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { AsyncPipe } from '@angular/common';
import { SettingService } from '../../setting/setting.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { AppInformation } from '../../setting/app-information';
import { AppConfiguration } from '../../setting/app-configuration';


type Version = { key: string; value: string };

@Component({
  selector: 'app-application-info',
  standalone: true,
  imports: [LoadingIndicatorComponent, AsyncPipe],
  templateUrl: './application-info.component.html',
})
export class ApplicationInfoComponent {
  private settingService = inject(SettingService);
  appVersions = toSignal(this.settingService.getAppInformation(), { initialValue: [] as AppInformation[] });
  appConfigs = toSignal(this.settingService.getAllAppSettings(), { initialValue: [] as AppConfiguration[] });
  isLoading = computed(() => !this.appVersions().length || !this.appConfigs().length);
}
