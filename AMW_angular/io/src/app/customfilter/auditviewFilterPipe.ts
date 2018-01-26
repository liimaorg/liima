import { Pipe, PipeTransform } from '@angular/core';
import * as _ from 'lodash';

@Pipe({
  name: 'auditviewFilter'
})
export class AuditviewFilterPipe implements PipeTransform {

  transform(array: any[], query: string): any {
    if (query) {
      return _.filter(array, row => this.allColumnsConcatenated(row).toLocaleLowerCase().indexOf(query.toLocaleLowerCase()) > -1);
    }
    return array;
  }

  private allColumnsConcatenated(row: any): string {
    return row.type + row.relation + row.name + row.username + row.oldValue + row.value + row.mode;
  }
}
