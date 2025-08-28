import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AppAddComponent } from './app-add.component';
import { signal } from '@angular/core';

describe('AppAddComponent', () => {
  let fixture: ComponentFixture<AppAddComponent>;
  let component: AppAddComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppAddComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(AppAddComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    // Provide default signals
    component.releases = signal([] as any);
    component.appServerGroups = signal([] as any);
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('hasInvalidGroup true when no group', () => {
    component.appServerGroup = undefined;
    expect(component.hasInvalidGroup()).toBeTrue();
  });

  it('hasInvalidGroup false when group with releases', () => {
    component.appServerGroup = { id: 1, releases: [{}] } as any;
    expect(component.hasInvalidGroup()).toBeFalse();
  });

  it('hasInvalidFields true when required missing', () => {
    component.app = { appName: '', appReleaseId: null, appServerId: null, appServerReleaseId: null };
    expect(component.hasInvalidFields()).toBeTrue();
  });

  it('save emits app and closes when valid', () => {
    component.app = { appName: 'a', appReleaseId: 1, appServerId: null, appServerReleaseId: null };
    spyOn(component.saveApp, 'emit');
    spyOn(activeModal, 'close');
    component.save();
    expect(component.saveApp.emit).toHaveBeenCalled();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('save does nothing when invalid', () => {
    component.app = { appName: '', appReleaseId: null, appServerId: null, appServerReleaseId: null };
    spyOn(component.saveApp, 'emit');
    spyOn(activeModal, 'close');
    component.save();
    expect(component.saveApp.emit).not.toHaveBeenCalled();
    expect(activeModal.close).not.toHaveBeenCalled();
  });

  it('cancel closes modal', () => {
    spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });
});
