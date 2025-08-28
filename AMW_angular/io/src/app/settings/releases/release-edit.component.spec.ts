import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';
import { DateModel } from '../../shared/date-picker/date.model';

describe('ReleaseEditComponent', () => {
  let fixture: ComponentFixture<ReleaseEditComponent>;
  let component: ReleaseEditComponent;
  let activeModal: NgbActiveModal;

  const baseRelease: Release = {
    id: null,
    name: 'rel',
    mainRelease: false,
    description: 'desc',
    installationInProductionAt: null,
    default: false,
    v: null,
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReleaseEditComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(ReleaseEditComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    component.release = { ...baseRelease };
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('getTitle returns Add when id missing', () => {
    component.release.id = null;
    expect(component.getTitle()).toBe('Add release');
  });

  it('getTitle returns Edit when id present', () => {
    component.release.id = 10 as any;
    expect(component.getTitle()).toBe('Edit release');
  });

  it('hasInvalidDate true when installationDate null', () => {
    component.installationDate = null;
    expect(component.hasInvalidDate()).toBeTrue();
  });

  it('cancel closes modal', () => {
    spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('save emits release and closes when valid date', () => {
    const epoch = 1234567890;
    component.installationDate = DateModel.fromEpoch(epoch);
    spyOn(component.saveRelease, 'emit');
    spyOn(activeModal, 'close');
    component.save();
    expect(component.saveRelease.emit).toHaveBeenCalled();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('save does nothing when date invalid', () => {
    component.installationDate = null;
    spyOn(component.saveRelease, 'emit');
    spyOn(activeModal, 'close');
    component.save();
    expect(component.saveRelease.emit).not.toHaveBeenCalled();
    expect(activeModal.close).not.toHaveBeenCalled();
  });
});
