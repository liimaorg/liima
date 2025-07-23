import { DateTimeModel } from '../shared/date-time-picker/date-time.model';
import { ComparatorFilterOption } from './comparator-filter-option';

export interface DeploymentFilter {
  name: string;
  comp: string;
  val: string | DateTimeModel;
  type: string;
  compOptions: ComparatorFilterOption[];
  valOptions: string[];
}
