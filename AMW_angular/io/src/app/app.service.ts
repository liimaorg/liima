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

// TODO: check if this Service could act as the Single Source of Truth... without using ngrx
@Injectable()
export class AppService {
  _state: InternalStateType = {};

  private _state$: BehaviorSubject<InternalStateType> = new BehaviorSubject({});

  get state$() {
    return this._state$;
  }

  constructor() {
    this._state$.next(this._state);
  }

  // already return a clone of the current state
  // deprecated - subscribe to the BehaviourSubject instead
  public get state() {
    return (this._state = this._clone(this._state));
  }
  // never allow mutation
  public set state(value) {
    throw new Error('do not mutate the `.state` directly');
  }

  // deprecated - subscribe to the BehaviourSubject instead
  public get(prop?: any) {
    // use our state getter for the clone
    const state = this.state;
    return state.hasOwnProperty(prop) ? state[prop] : state;
  }

  // TODO: refactor to accept an object... maybe an action with payload
  public set(prop: string, value: any) {
    // internally mutate our state
    this._state[prop] = value;
    // emit new state for subscribers..
    this._state$.next(this._state);
    // return the state for backwards compatibility
    return this._state;
  }

  private _clone(object: InternalStateType) {
    // simple object clone
    return JSON.parse(JSON.stringify(object));
  }
}
