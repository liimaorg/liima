import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {ToastComponent} from './toast.component';


describe('ToastComponent', () => {
  let component: ToastComponent;
  let fixture: ComponentFixture<ToastComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ToastComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ToastComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display toast if show is true', () => {
    component.show = true;
    fixture.detectChanges();
    const toastElement = fixture.nativeElement.querySelector('.toast');
    expect(toastElement).not.toBeNull();
  });

  it('should display toast if show is false', () => {
    component.show = false;
    fixture.detectChanges();
    const toastElement = fixture.nativeElement.querySelector('.toast');
    expect(toastElement).toBeNull();
  });

  it('should display message', () => {
    const testMessage = 'Test message';
    component.display(testMessage);
    fixture.detectChanges();
    const messageElement = fixture.nativeElement.querySelector('.toast-body li').textContent;
    expect(messageElement).toContain(testMessage);
  });

  it('should hide toast', (done) => {
    const testMessage = 'Test message';
    const duration = 1000;
    component.display(testMessage, duration);
    fixture.detectChanges();
    setTimeout(() => {
      fixture.detectChanges();
      const toastElement = fixture.nativeElement.querySelector('.toast');
      expect(toastElement).toBeNull();
      done();
    }, duration + 100); // adding a bit of extra time to ensure toast is gone
  });
});
