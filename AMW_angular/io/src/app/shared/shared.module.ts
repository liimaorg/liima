import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortableIconComponent } from './sortable-icon/sortable-icon.component';

import { LoadingIndicatorComponent } from './elements/loading-indicator.component';
import { PageNotFoundComponent } from './page-not-found.component';
import { PaginationComponent } from './pagination/pagination.component';
import { FormsModule } from '@angular/forms';
import { NotificationComponent } from './elements/notification/notification.component';
import { DateTimePickerComponent } from './date-time-picker/date-time-picker.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { IconComponent } from './icon/icon.component';

@NgModule({
  declarations: [
    SortableIconComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent,
    PaginationComponent,
    NotificationComponent,
    DateTimePickerComponent,
    IconComponent,
  ],
  imports: [CommonModule, FormsModule, NgbModule],
  exports: [
    SortableIconComponent,
    PageNotFoundComponent,
    LoadingIndicatorComponent,
    PaginationComponent,
    NotificationComponent,
    DateTimePickerComponent,
    IconComponent,
  ],
})
export class SharedModule {}
