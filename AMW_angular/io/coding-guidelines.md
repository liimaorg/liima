# Coding Guidelines

## Inject() over Constructor-Injections

Use `inject()` instead of constructor-injection to make the code more explicit and obvious.

```typescript
// use
myService = inject(MyService);

// instead of
constructor(
  private myservice: MyService
) {}
```

## RxJS

Leverage RxJS for API calls, web sockets, and complex data flows, especially when handling multiple asynchronous events. Combine with Signals to simplify component state management.

## Signals

Use Signals for changing values in Components

### Signal from Observable

#### GET

Make API-requests with observables and expose the state as a Signal:

```typescript
// retrieve data from API using RxJS
private users$ = this.http.get<User[]>(this.userUrl);

// expose as Signal
users = toSignal(this.users$, { initialValue: [] as User[]});
```

The observable `users$` is just used to pass the state to the _readonly_ Signal `users`. (The Signals created from an observable are always _readonly_!)
No need for unsubscription - this is handled by `toSignal()` automatically.

Use the Signal in the component not in the template. (Separation of concerns)

#### POST / DELETE etc.

To update data in a Signal you have to create a WritableSignal:

```typescript
// WritableSignal
users = signal<Tag[]>([]);
// retrieve data from API using RxJS and write it in the WritableSignal
private users$ = this.http.get<User[]>(this.userUrl).pipe(tap((users) => this.users.set(users)));
// only used to automatically un-/subscribe to the observable
readOnlyUsers = toSignal(this.users$, { initialValue: [] as User[]});
```

## Auth Service

The frontend provides a singelton auth-service which holds all restrictions for the current user.

After injecting the service in your component you can get Permissions/Actions depending on your needs:

```typescript
// inject the service
authService = inject(AuthService);

// get actions for a specific permission
const actions = this.authService.getActionsForPermission('MY_PERMISSION');

// verify role in an action and set Signal
this.canCreate.set(actions.some(isAllowed('CREATE')));

// or directly set Signal based on a concrete permission and action value
this.canViewSettings.set(this.authService.hasPermission('SETTINGS', 'READ'));
```

## Buttons

Buttons without a text should have a html title set.<br>
This improves accessibility and user experience, as the title serves as a tooltip that provides brief information about the button's function.
