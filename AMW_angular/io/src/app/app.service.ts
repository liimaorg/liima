import { Injectable } from '@angular/core';
import { NavigationItem } from './core/navigation-item';
import { Subject, BehaviorSubject } from 'rxjs';

export type InternalStateType = {
  [key: string]: any;
};

export enum Keys {
  NavItems = 'navItems',
  NavTitle = 'navTitle',
  PageTitle = 'pageTitle',
  LogoutUrl = 'logoutUrl',
  NavShow = 'navShow'
}

@Injectable()
export class AppService {
  _state: InternalStateType = {};

  //private _state$: Subject<InternalStateType> = new BehaviourSubject();

  private _state$: BehaviorSubject<InternalStateType> = new BehaviorSubject({});

  get state$() {
    return this._state$;
  }

  constructor() {
    this._state$.next(this._state);
  }

  // already return a clone of the current state
  public get state() {
    return (this._state = this._clone(this._state));
  }
  // never allow mutation
  public set state(value) {
    throw new Error('do not mutate the `.state` directly');
  }

  public get(prop?: any) {
    // use our state getter for the clone
    const state = this.state;
    return state.hasOwnProperty(prop) ? state[prop] : state;
  }

  public set(prop: string, value: any) {
    // internally mutate our state
    this._state[prop] = value;
    // emit new state for subscribers..
    this._state$.next(this._state);
    // return the state for backwards compatibility
    return this._state;
  }

  // public navItems(): NavigationItem[] {
  //   if (this.get(Keys.NavItems)) {
  //     return Array.from(this.get(Keys.NavItems));
  //   }
  //   return [];
  // }

  private _clone(object: InternalStateType) {
    // simple object clone
    return JSON.parse(JSON.stringify(object));
  }
}
