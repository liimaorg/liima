export interface Release {
  name: string;
  mainRelease: boolean;
  description: string;
  installationInProductionAt: number;
  id: number;
  v: number;
  default: boolean;
}
