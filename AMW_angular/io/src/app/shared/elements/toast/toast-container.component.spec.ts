import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ToastContainerComponent } from './toast-container.component';

describe('ToastContainerComponent', () => {
  let component: ToastContainerComponent;
  let fixture: ComponentFixture<ToastContainerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ToastContainerComponent],
    });
    fixture = TestBed.createComponent(ToastContainerComponent);
    component = TestBed.inject(ToastContainerComponent);
  });

  it('should create the component', () => {
    expect(component).toBeDefined();
  });

  it('should show the toast', () => {
    component.toastService.success('success-message');
    fixture.detectChanges();
    const element = fixture.nativeElement;
    const message = element.querySelector('.toast-body .content');
    expect(message.textContent).toBe('success-message');
  });
});
