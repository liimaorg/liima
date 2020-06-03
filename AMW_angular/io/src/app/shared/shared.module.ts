import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortableIconComponent } from './sortable-icon/sortable-icon.component';
import { PageHeaderComponent } from './elements/page-header.component';

import { LoadingIndicatorComponent } from './elements/loading-indicator.component';
import { PageNotFoundComponent } from './page-not-found.component';
import { PanelHeadingComponent } from './panel-heading/panel-heading.component';
import { PaginationComponent } from './pagination/pagination.component';
import { FormsModule } from '@angular/forms';
import { NotificationComponent } from './elements/notification/notification.component';

@NgModule({
  declarations: [
    SortableIconComponent,
    PageHeaderComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent,
    PanelHeadingComponent,
    PaginationComponent,
    NotificationComponent
  ],
  imports: [CommonModule, FormsModule],
  exports: [
    SortableIconComponent,
    PageHeaderComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent,
    PanelHeadingComponent,
    PaginationComponent,
    NotificationComponent
  ]
})
export class SharedModule {}
