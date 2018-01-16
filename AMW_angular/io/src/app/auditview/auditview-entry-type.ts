export interface Auditviewentrytype {
  timestamp: number;
  type: string; // Property, Resource, ...
  name: string; // Property Descriptor, ...
  username: string;
  oldValue: string;
  value: string;
  revision: number;
  mode: string;
  editContextName: string;
  relation: string;
}
