import { ToastService } from './toast-service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    service = new ToastService();
  });

  it('#success should add a toast with type success', () => {
    service.success('success-message');
    expect(service.toasts.length).toBe(1);
    const toast = service.toasts[0];
    expect(toast.type).toBe('success');
    expect(toast.body).toBe('success-message');
    expect(toast.delay).toBe(5000);
  });

  it('#error should add a toast with type error and longer delay', () => {
    service.error('error-message');
    const toast = service.toasts[0];
    expect(toast.type).toBe('error');
    expect(toast.body).toBe('error-message');
    expect(toast.delay).toBe(15000);
  });

  it('#show should add a custom toast', () => {
    service.show({ body: 'custom-message', type: 'success', delay: 100 });

    const toast = service.toasts[0];
    expect(toast.type).toBe('success');
    expect(toast.body).toBe('custom-message');
    expect(toast.delay).toBe(100);
  });

  it('#remove should remove a toast', () => {
    service.success('success-message');
    service.error('error-message');
    expect(service.toasts.length).toBe(2);
    service.remove(service.toasts[0]);
    expect(service.toasts.length).toBe(1);
  });
});
