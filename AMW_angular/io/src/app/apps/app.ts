import { Release } from '../settings/releases/release';

export interface App {
  id: number;
  name: string;
  release: Release;
}
