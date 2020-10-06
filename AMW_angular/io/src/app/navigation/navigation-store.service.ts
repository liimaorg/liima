import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { NavigationItem } from './navigation-item';

export interface Navigation {
  pageTitle: string;
  current: string;
  items: NavigationItem[];
  logoutUrl: string;
  visible: boolean;
}

const initial = {
  pageTitle: '',
  current: '',
  items: [],
  logoutUrl: '',
  visible: false,
};

// inspired by https://dev.to/avatsaev/simple-state-management-in-angular-with-only-services-and-rxjs-41p8
@Injectable({
  providedIn: 'root',
})
export class NavigationStoreService {
  // - We set the initial state in BehaviorSubject's constructor
  // - Nobody outside the Store should have access to the BehaviorSubject
  //   because it has the write rights
  // - Writing to state should be handled by specialized Store methods (setPageTitle, setItems)
  private readonly _navigation = new BehaviorSubject<Navigation>(initial);

  // Expose the observable$ part of the _navigation subject (read only stream)
  readonly navigation$ = this._navigation.asObservable();

  get navigation(): Navigation {
    return this._navigation.getValue();
  }

  set navigation(nav: Navigation) {
    this._navigation.next(nav);
  }

  setPageTitle(pageTitle: string) {
    this.navigation = { ...this.navigation, pageTitle };
  }

  setCurrent(current: string) {
    this.navigation = { ...this.navigation, current };
  }

  setItems(items: NavigationItem[]) {
    this.navigation = { ...this.navigation, items };
  }

  setLogoutUrl(logoutUrl: string) {
    this.navigation = { ...this.navigation, logoutUrl };
  }

  setVisible(visible: boolean) {
    this.navigation = { ...this.navigation, visible };
  }
}
