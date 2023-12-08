import { Injectable, Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'newlineFilter',
    standalone: true
})

@Injectable()
export class NewlineFilterPipe implements PipeTransform {
  transform(value: string): string {
    return value.replace(/(?:\r\n|\r|\n)/g, '<br />');
  }
}
