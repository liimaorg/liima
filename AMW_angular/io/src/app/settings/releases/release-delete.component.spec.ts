import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ReleaseDeleteComponent } from './release-delete.component';

describe('ReleaseDeleteComponent', () => {
  let fixture: ComponentFixture<ReleaseDeleteComponent>;
  let component: ReleaseDeleteComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReleaseDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(ReleaseDeleteComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    component.resources = new Map();
    component.release = { id: null } as any;
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('sets hasResources true when resources provided on init', () => {
    component.resources = new Map([['k', [{ id: 1 } as any]]]);
    component.ngOnInit();
    expect(component.hasResources).toBe(true);
  });

  it('getTitle returns Remove release', () => {
    expect(component.getTitle()).toBe('Remove release');
  });

  it('cancel closes modal', () => {
    vi.spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('delete emits deleteRelease and closes', () => {
    vi.spyOn(component.deleteRelease, 'emit');
    vi.spyOn(activeModal, 'close');
    component.delete();
    expect(component.deleteRelease.emit).toHaveBeenCalledWith(component.release);
    expect(activeModal.close).toHaveBeenCalled();
  });
});
