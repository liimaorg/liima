import { Release } from '../settings/releases/release';
import { App } from './app';

export interface AppServer {
  id: number;
  name: string;
  deletable: boolean;
  runtimeName: string;
  release: Release;
  apps: App[];
}
