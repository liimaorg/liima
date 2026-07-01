export interface Release {
  name: string;
  mainRelease: boolean;
  description: string;
  installationInProductionAt: number | null; // e.g. 1893542400000. Post also supports ISO format, but we use epoch for simplicity.
  id: number | null;
  v: number | null;
  default: boolean;
}
