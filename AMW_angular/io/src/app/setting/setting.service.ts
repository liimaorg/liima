import { Http, Response } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { AppConfiguration } from './app-configuration';
import { BaseService } from '../base/base.service';

@Injectable()
export class SettingService extends BaseService {

  constructor(private http: Http) {
    super();
  }

  getAllAppSettings(): Observable<AppConfiguration[]> {
    const resource$ = this.http
      .get(`${this.getBaseUrl()}/settings`, {headers: this.getHeaders()})
      .map((response: Response) => this.extractPayload(response))
      .catch(this.handleError);
    return resource$;
  }

}
