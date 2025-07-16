import { ChangeDetectionStrategy, Component, input, output, OnChanges, SimpleChanges } from '@angular/core';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Environment } from '../../deployment/environment';
import { Resource } from '../../resource/resource';
import { ServerFilter } from './server-filter';
import { NgbTypeahead } from '@ng-bootstrap/ng-bootstrap';
import { Observable, OperatorFunction } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/operators';
import { ButtonComponent } from '../../shared/button/button.component';

@Component({
  selector: 'app-servers-filter',
  imports: [NgSelectModule, ReactiveFormsModule, FormsModule, NgbTypeahead, ButtonComponent],
  templateUrl: './servers-filter.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServersFilterComponent implements OnChanges {
  environments = input.required<Environment[]>();
  runtimes = input.required<Resource[]>();
  appServerSuggestions = input.required<string[]>();
  inputSearchFilter = input<ServerFilter>();
  searchFilter = output<ServerFilter>();
  filter: ServerFilter = {
    environmentName: 'All',
    runtimeName: 'All',
    appServer: null,
    host: null,
    node: null
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['inputSearchFilter'] && this.inputSearchFilter()) {
      const input = this.inputSearchFilter();
      this.filter = {
        environmentName: input.environmentName ?? 'All',
        runtimeName: input.runtimeName ?? 'All',
        appServer: input.appServer ?? null,
        host: input.host ?? null,
        node: input.node ?? null
      };
    }
  }

  search: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      map((term) =>
        term.length < 1
          ? []
          : this.appServerSuggestions()
              .filter((v) => v.toLowerCase().indexOf(term.toLowerCase()) > -1)
              .slice(0, 10),
      ),
    );

  searchServerFilter() {
    this.searchFilter.emit(this.filter);
  }

  reset() {
    this.filter = {
      environmentName: 'All',
      runtimeName: 'All',
      appServer: null,
      host: null,
      node: null
    };
  }
}
