import { ComparatorFilterOption } from './comparator-filter-option';

export interface DeploymentFilter {
  name: string;
  comp: string;
  val: string;
  type: string;
  compOptions: ComparatorFilterOption[];
  valOptions: string[];
}
