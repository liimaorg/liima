import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from './navigation.component';
import { NavigationSidebarComponent } from './navigation-sidebar.component';

@NgModule({
  declarations: [NavigationComponent, NavigationSidebarComponent],
  exports: [NavigationComponent, NavigationSidebarComponent],
  imports: [CommonModule]
})
export class NavigationModule {}
