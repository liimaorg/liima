import { Component } from '@angular/core';
import { LoadingIndicatorComponent } from "../../shared/elements/loading-indicator.component";
import { HttpClient } from "@angular/common/http";
import {NgFor, NgIf} from "@angular/common";

type Config = { key: { value: string, env: string }; value: string; defaultValue: string };
type Version = { key: string; value: string; };

@Component({
  selector: 'amw-application-info',
  standalone: true,
  imports: [LoadingIndicatorComponent,     NgIf,NgFor,],
  templateUrl: './application-info.component.html',
  styleUrl: './application-info.component.scss'
})
export class ApplicationInfoComponent {
  isLoading: boolean = false;

  appVersions: Version[] = [];
  appConfigs: Config[] = [];

  constructor(private http: HttpClient) {
    http.get<Version[]>('/AMW_rest/resources/settings/appInfo').subscribe({
      next: (data) => {
        this.appVersions = data;
      },
    });
    http.get<Config[]>('/AMW_rest/resources/settings').subscribe({
      next: (data) => {
        this.appConfigs = data;
      },
    });
  }
}
