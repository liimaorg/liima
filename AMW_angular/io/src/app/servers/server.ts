export type Server = {
  host: string;
  appServer: string;
  appServerRelease: string;
  runtime: string;
  node: string;
  nodeRelease: string;
  environment: string;
  appServerId: number;
  nodeId: number;
  environmentId: number;
  domain: string;
  domainId: string;
  definedOnNode: boolean;
};
