import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortableIconComponent } from './sortable-icon/sortable-icon.component';
import { PageHeaderComponent } from './elements/page-header.component';

import { LoadingIndicatorComponent } from './elements/loading-indicator.component';
import { PageNotFoundComponent } from './page-not-found.component';
import { PanelHeadingComponent } from './panel-heading/panel-heading.component';

@NgModule({
  declarations: [
    SortableIconComponent,
    PageHeaderComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent,
    PanelHeadingComponent
  ],
  imports: [CommonModule],
  exports: [
    SortableIconComponent,
    PageHeaderComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent,
    PanelHeadingComponent
  ]
})
export class SharedModule {}
