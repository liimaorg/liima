import { AuditviewComponent } from './auditview.component';

const AUDIT_VIEW_TITLE = 'Audit View - Liima';
export const auditviewRoutes = [
  { path: 'auditview', component: AuditviewComponent, title: AUDIT_VIEW_TITLE },
  { path: 'auditview/:resourceId', component: AuditviewComponent, title: AUDIT_VIEW_TITLE },
];
