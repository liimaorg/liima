import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from './navigation.component';
import { NavigationSidebarComponent } from './navigation-sidebar.component';
import {NgbCollapseModule} from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  declarations: [NavigationComponent, NavigationSidebarComponent],
  exports: [NavigationComponent, NavigationSidebarComponent],
  imports: [CommonModule, NgbCollapseModule]
})
export class NavigationModule {}
