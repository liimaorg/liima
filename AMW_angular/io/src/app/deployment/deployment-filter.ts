import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { ComparatorFilterOption } from './comparator-filter-option';

export interface DeploymentFilter {
  name: string;
  comp: string;
  val: any;
  type: string;
  compOptions: ComparatorFilterOption[];
  valOptions: string[];
}
