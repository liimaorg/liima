import { RouterModule } from '@angular/router';
import { AuditviewComponent } from './auditview.component';
import { NgModule } from '@angular/core';

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'auditview', component: AuditviewComponent},
    {path: 'auditview/:resourceId', component: AuditviewComponent},
  ])],
  exports: [RouterModule]
})
export class AuditviewRoutingModule {
}
