import { RouterModule } from '@angular/router';
import { AuditviewComponent } from './auditview.component';
import { NgModule } from '@angular/core';
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [RouterModule.forChild([
    {path: 'auditview', component: AuditviewComponent},
    {path: 'auditview/:resourceId', component: AuditviewComponent},
  ]), NgbModule],
  exports: [RouterModule]
})
export class AuditviewRoutingModule {
}
