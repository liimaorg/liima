import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { TagsComponent } from './tags/tags.component';
import { SettingsComponent } from './settings.component';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'settings',
        component: SettingsComponent,
        title: 'Settings',
        children: [{ title: 'Settings - Tags', path: '', component: TagsComponent }],
      },
    ]),
  ],
  exports: [RouterModule],
})
export class SettingsRoutingModule {}
