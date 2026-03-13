import { PropertyDescriptorOrigin } from './property';

export interface PropertyDiff {
  origin: PropertyDescriptorOrigin;
  env: string;
  val: string;
}
