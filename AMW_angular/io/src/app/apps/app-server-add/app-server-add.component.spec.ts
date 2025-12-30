import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AppServerAddComponent } from './app-server-add.component';
import { signal } from '@angular/core';

describe('AppServerAddComponent', () => {
  let fixture: ComponentFixture<AppServerAddComponent>;
  let component: AppServerAddComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppServerAddComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(AppServerAddComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    component.releases = signal([] as any);
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('hasInvalidFields true when missing name', () => {
    component.appServer.name = '';
    component.appServer.release = { id: 1 } as any;
    expect(component.hasInvalidFields()).toBe(true);
  });

  it('hasInvalidFields true when missing release', () => {
    component.appServer.name = 'srv';
    component.appServer.release = null;
    expect(component.hasInvalidFields()).toBe(true);
  });

  it('hasInvalidFields false when name and release present', () => {
    component.appServer.name = 'srv';
    component.appServer.release = { id: 2 } as any;
    expect(component.hasInvalidFields()).toBe(false);
  });

  it('save emits and closes when valid', () => {
    component.appServer.name = 'srv';
    component.appServer.release = { id: 2 } as any;
    vi.spyOn(component.saveAppServer, 'emit');
    vi.spyOn(activeModal, 'close');
    component.save();
    expect(component.saveAppServer.emit).toHaveBeenCalled();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('save does nothing when invalid', () => {
    component.appServer.name = '';
    component.appServer.release = null;
    vi.spyOn(component.saveAppServer, 'emit');
    vi.spyOn(activeModal, 'close');
    component.save();
    expect(component.saveAppServer.emit).not.toHaveBeenCalled();
    expect(activeModal.close).not.toHaveBeenCalled();
  });

  it('cancel closes modal', () => {
    vi.spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });
});
