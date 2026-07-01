export type ServerFilter = {
  environmentName?: string | null;
  runtimeName?: string | null;
  appServer?: string | null;
  host?: string | null;
  node?: string | null;
};
export function isServerFilterEmpty(filter: ServerFilter | undefined | null): boolean {
  if (filter)
    return (
      (!filter.environmentName || filter.environmentName === 'All' || filter.environmentName.trim().length === 0) &&
      (!filter.runtimeName || filter.runtimeName === 'All' || filter.runtimeName.trim().length === 0) &&
      (!filter.appServer || filter.appServer.trim().length === 0) &&
      (!filter.host || filter.host.trim().length === 0) &&
      (!filter.node || filter.node.trim().length === 0)
    );
  return true;
}
