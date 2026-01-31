import { DateTimeModel } from '../shared/date-time-picker/date-time.model';

export type FilterValue = string | number | boolean | DateTimeModel;

export interface DeploymentFilter {
  name: string;
  comp: string;
  val: FilterValue;
}
