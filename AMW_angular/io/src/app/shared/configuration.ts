export type Config = { key: { value: string; env: string }; value: string; defaultValue: string };

export function pluck(key: string, config: Config[]): string {
  const found = config.find((c) => c.key.value === key);
  return found ? found.value : undefined;
}
