import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
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
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServersFilterComponent {
  environments = input.required<Environment[]>();
  runtimes = input.required<Resource[]>();
  appServerSuggestions = input.required<string[]>();
  searchFilter = output<ServerFilter>();
  selectedEnvironmentName: string = 'All';
  selectedRuntimeName: string = 'All';
  appServer: string;
  host: string;
  node: string;

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
    const filter: ServerFilter = {
      environmentName: this.selectedEnvironmentName,
      runtimeName: this.selectedRuntimeName,
      appServer: this.appServer,
      host: this.host,
      node: this.node,
    };
    this.searchFilter.emit(filter);
  }

  reset() {
    this.selectedEnvironmentName = 'All';
    this.selectedRuntimeName = 'All';
    this.appServer = null;
    this.host = null;
    this.node = null;
  }
}
