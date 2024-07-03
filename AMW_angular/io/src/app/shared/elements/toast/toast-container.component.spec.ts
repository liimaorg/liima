import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ToastContainerComponent } from './toast-container.component';
import { ToastService } from './toast-service';

describe('ToastContainerComponent', () => {
  let component: ToastContainerComponent;
  let toastService: ToastService;
  let fixture: ComponentFixture<ToastContainerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ToastContainerComponent, { provide: ToastService, useClass: ToastService }],
    });
    fixture = TestBed.createComponent(ToastContainerComponent);
    component = TestBed.inject(ToastContainerComponent);
    toastService = TestBed.inject(ToastService);
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
