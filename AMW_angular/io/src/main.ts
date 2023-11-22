import { enableProdMode, importProvidersFrom } from '@angular/core';

import { environment } from './environments/environment';
import { AppComponent } from './app/app.component';
import { SettingsModule } from './app/settings/settings.module';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { BaseModule } from './app/base/base.module';
import { SettingModule } from './app/setting/setting.module';
import { PermissionModule } from './app/permission/permission.module';
import { AuditviewModule } from './app/auditview/auditview.module';
import { DeploymentsModule } from './app/deployments/deployments.module';
import { DeploymentModule } from './app/deployment/deployment.module';
import { ResourceModule } from './app/resource/resource.module';
import { AppRoutingModule } from './app/app-routing.module';
import { withInterceptorsFromDi, provideHttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { provideAnimations } from '@angular/platform-browser/animations';
import { BrowserModule, bootstrapApplication } from '@angular/platform-browser';
import { NavigationStoreService } from './app/navigation/navigation-store.service';

const APP_PROVIDERS = [NavigationStoreService];

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(
      BrowserModule,
      NgSelectModule,
      FormsModule,
      AppRoutingModule,
      ResourceModule,
      DeploymentModule,
      DeploymentsModule,
      AuditviewModule,
      PermissionModule,
      SettingModule,
      BaseModule,
      NgbModule,
      CodemirrorModule,
      SettingsModule,
    ),
    APP_PROVIDERS,
    provideAnimations(),
    provideHttpClient(withInterceptorsFromDi()),
  ],
}).catch((err) => console.error(err));
