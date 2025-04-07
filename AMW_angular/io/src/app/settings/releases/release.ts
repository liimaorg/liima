export interface Release {
  name: string;
  mainRelease: boolean;
  description: string;
  installationInProductionAt: string;
  id: number;
  v: number;
  default: boolean;
}
