import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { NgSelectModule } from '@ng-select/ng-select';
/*
 * Feature Modules
 */
import { ResourceModule } from './resource/resource.module';
import { DeploymentModule } from './deployment/deployment.module';
import { PermissionModule } from './permission/permission.module';
import { AuditviewModule } from './auditview/auditview.module';
import { SettingModule } from './setting/setting.module';
import { BaseModule } from './base/base.module';
import { SharedModule } from './shared/shared.module';
/*
 * Platform and Environment providers/directives/pipes
 */
import { AppRoutingModule } from './app-routing.module';
// App is our top level component
import { AppComponent } from './app.component';
import { AppService, InternalStateType } from './app.service';
import { APP_RESOLVER_PROVIDERS } from './app.resolver';
import { NavigationComponent } from './navigation.component';
import { NavigationSidebarComponent } from './navigation-sidebar.component';
import { NavigationModule } from './navigation/navigation.module';

// Application wide providers
const APP_PROVIDERS = [...APP_RESOLVER_PROVIDERS, AppService];

type StoreType = {
  state: InternalStateType;
  restoreInputValues: () => void;
  disposeOldHosts: () => void;
};

/**
 * `AppModule` is the main entry point into Angular2's bootstraping process
 */
@NgModule({
  declarations: [AppComponent],
  imports: [
    // import Angular's modules
    BrowserModule,
    BrowserAnimationsModule,
    NgSelectModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    ResourceModule,
    DeploymentModule,
    AuditviewModule,
    PermissionModule,
    SettingModule,
    BaseModule,
    SharedModule,
    NavigationModule
  ],
  providers: [
    // expose our Services and Providers into Angular's dependency injection
    APP_PROVIDERS
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
