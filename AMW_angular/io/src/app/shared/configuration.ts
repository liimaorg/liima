export type Config = { key: { value: string; env: string }; value: string; defaultValue: string };

export function pluck(key: string, config: Config[]): string {
  return config.filter((c) => c.key.value === key).map((c) => c.key.value)[0];
}
