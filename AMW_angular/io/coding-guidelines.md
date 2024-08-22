# Coding Guidelines

## Inject() over Constructor-Injections

Use `inject()` instead of constuctor-injection to make the code more explicit and obvious.

```typescript
// use
myService = inject(MyService);

// instead of
constructor(
  private myservice: MyService
) {}
```

## Signals

Use signals for changing values in Components

### Signal from Observable

#### GET

Make API-requests with observables and expose the state as a signal:

```typescript
// retrive data from API using RxJS
private users$ = this.http.get<User[]>(this.userUrl);

// expose as signal
users = toSignal(this.users$, { initialValue: [] as User[]});
```

The observable `users$` is just used to pass the state to the _readonly_ signal `users`. (The signals created from an observable are always _readonly_!)
No need for unsubscription - this is handled by `toSignal()` automatically.

Use the signal in the component not in the template. (Separation of concerns)

#### POST / DELETE etc.

To update data in a signal you have to create a WritableSignal:

```typescript
// WritableSignal
users = signal<Tag[]>([]);
// retrive data from API using RxJS and write it in the WritableSignal
private users$ = this.http.get<User[]>(this.userUrl).pipe(tap((users) => this.users.set(users)));
// only used to automatically un-/subscribe to the observable
readOnlyUsers = toSignal(this.users$, { initialValue: [] as User[]});
```
