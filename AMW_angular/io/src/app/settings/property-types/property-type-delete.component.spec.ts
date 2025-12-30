import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PropertyTypeDeleteComponent } from './property-type-delete.component';

describe('PropertyTypeDeleteComponent', () => {
  let fixture: ComponentFixture<PropertyTypeDeleteComponent>;
  let component: PropertyTypeDeleteComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyTypeDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(PropertyTypeDeleteComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    // Initialize required @Input used in template before first change detection
    component.propertyType = { name: 'Test PT', id: 1, validationRegex: '', encrypted: false, propertyTags: [] } as any;
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('returns correct title', () => {
    expect(component.getTitle()).toBe('Remove property type');
  });

  it('calls activeModal.close on cancel()', () => {
    vi.spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('emits deletePropertyType and closes on delete()', () => {
    const pt: any = { id: 123 };
    component.propertyType = pt;
    vi.spyOn(component.deletePropertyType, 'emit');
    vi.spyOn(activeModal, 'close');
    component.delete();
    expect(component.deletePropertyType.emit).toHaveBeenCalledWith(pt);
    expect(activeModal.close).toHaveBeenCalled();
  });
});
