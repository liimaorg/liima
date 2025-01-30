import { Component, computed, inject } from '@angular/core';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { SettingService } from '../../setting/setting.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { AppInformation } from '../../setting/app-information';
import { AppConfiguration } from '../../setting/app-configuration';
import { TableComponent, TableColumnType } from '../../shared/table/table.component';

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

  appConfigsTableData = computed(() =>
    this.appConfigs().map((config) => {
      return {
        keyValue: config.key.value,
        keyEnv: config.key.env,
        value: config.value,
        defaultValue: config.defaultValue,
      };
    }),
  );

  isLoading = computed(() => !this.appVersions().length || !this.appConfigs().length);

  appVersionsHeader(): TableColumnType<AppInformation>[] {
    return [
      {
        key: 'key',
        columnTitle: 'Key',
      },
      {
        key: 'value',
        columnTitle: 'Value',
      },
    ];
  }

  appConfigsHeader(): TableColumnType<{
    keyValue: string;
    keyEnv: string;
    value: string;
    defaultValue: string;
  }>[] {
    return [
      {
        key: 'keyValue',
        columnTitle: 'Key',
      },
      {
        key: 'keyEnv',
        columnTitle: 'ENV',
      },
      {
        key: 'value',
        columnTitle: 'Value',
      },
      {
        key: 'defaultValue',
        columnTitle: 'Default value',
      },
    ];
  }
}
