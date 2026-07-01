import { ToastService } from './toast.service';
import { HttpErrorResponse } from '@angular/common/http';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    service = new ToastService();
  });

  it('#success should add a toast with type success', async () => {
    service.success('success-message');
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(service.toasts().length).toBe(1);
    const toast = service.toasts()[0];
    expect(toast.type).toBe('success');
    expect(toast.body).toBe('success-message');
    expect(toast.delay).toBe(5000);
  });

  it('#error should add a toast with type error and longer delay', async () => {
    service.error('error-message');
    await new Promise((resolve) => setTimeout(resolve, 0));
    const toast = service.toasts()[0];
    expect(toast.type).toBe('error');
    expect(toast.body).toBe('error-message');
    expect(toast.delay).toBe(15000);
  });

  it('#error should extract message from HttpErrorResponse', async () => {
    const httpError = new HttpErrorResponse({ error: { message: 'backend failed' }, status: 422 });
    service.error(httpError);
    await new Promise((resolve) => setTimeout(resolve, 0));
    const toast = service.toasts()[0];
    expect(toast.type).toBe('error');
    expect(toast.body).toBe('backend failed');
  });

  it('#error should not render [object Object] for plain error objects', async () => {
    service.error({ error: { message: 'conflict detected' } });
    await new Promise((resolve) => setTimeout(resolve, 0));
    const toast = service.toasts()[0];
    expect(toast.body).toBe('conflict detected');
  });

  it('#error should fall back to a generic message for unknown objects', async () => {
    service.error({ foo: 'bar' });
    await new Promise((resolve) => setTimeout(resolve, 0));
    const toast = service.toasts()[0];
    expect(toast.body).toBe('An error occurred');
  });

  it('#show should add a custom toast', async () => {
    service.show({ body: 'custom-message', type: 'success', delay: 100 });
    await new Promise((resolve) => setTimeout(resolve, 0));

    const toast = service.toasts()[0];
    expect(toast.type).toBe('success');
    expect(toast.body).toBe('custom-message');
    expect(toast.delay).toBe(100);
  });

  it('#remove should remove a toast', async () => {
    service.success('success-message');
    service.error('error-message');
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(service.toasts().length).toBe(2);
    service.remove(service.toasts()[0]);
    expect(service.toasts().length).toBe(1);
  });
});
