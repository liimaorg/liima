import { Component, computed, inject } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { SettingService } from '../../setting/setting.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { AppInformation } from '../../setting/app-information';
import { AppConfiguration } from '../../setting/app-configuration';
import { TableComponent, TableHeader } from '../../shared/table/table.component';

@Component({
  selector: 'app-application-info',
  standalone: true,
  imports: [LoadingIndicatorComponent, TableComponent],
  templateUrl: './application-info.component.html',
})
export class ApplicationInfoComponent {
  private settingService = inject(SettingService);
  appVersions = toSignal(this.settingService.getAppInformation(), { initialValue: [] as AppInformation[] });
  appConfigs = toSignal(this.settingService.getAllAppSettings(), { initialValue: [] as AppConfiguration[] });
  isLoading = computed(() => !this.appVersions().length || !this.appConfigs().length);

  appVersionsHeader(): TableHeader[] {
    return [
      {
        key: 'key',
        title: 'Key',
      },
      {
        key: 'value',
        title: 'Value',
      },
    ];
  }

  appConfigsHeader(): TableHeader[] {
    return [
      {
        key: 'key',
        title: 'key',
        type: 'split',
        nested: [
          { key: 'value', title: 'Key' },
          { key: 'env', title: 'ENV' },
        ],
      },
      {
        key: 'value',
        title: 'Value',
      },
      {
        key: 'defaultValue',
        title: 'Default value',
      },
    ];
  }
}
