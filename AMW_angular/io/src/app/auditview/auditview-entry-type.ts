export interface Auditviewentrytype {
  timestamp: number;
  type: string; // Property, Resource, ...
  name: string; // Property Descriptor, ...
  username: string;
  value: string;
  revision: number;
  mode: string;
  environment: string;
}
