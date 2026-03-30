import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PropertyTypeEditComponent } from './property-type-edit.component';
import { PropertyType } from './property-type';

describe('PropertyTypeEditComponent', () => {
  let fixture: ComponentFixture<PropertyTypeEditComponent>;
  let component: PropertyTypeEditComponent;
  let activeModal: NgbActiveModal;

  const basePropertyType: PropertyType = {
    id: null,
    name: '',
    validationRegex: '',
    encrypted: false,
    propertyTags: [],
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PropertyTypeEditComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(PropertyTypeEditComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    component.propertyType = { ...basePropertyType };
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('getTitle returns Add when id missing', () => {
    component.propertyType.id = null;
    expect(component.getTitle()).toContain('Add');
  });

  it('getTitle returns Edit when id present', () => {
    component.propertyType.id = 5 as any;
    expect(component.getTitle()).toContain('Edit');
  });

  it('cancel closes modal', () => {
    vi.spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('isValidRegex handles empty regex', () => {
    component.propertyType.validationRegex = '';
    expect(component.isValidRegex()).toBe(true);
  });

  it('isValidRegex returns false on invalid regex', () => {
    component.propertyType.validationRegex = '['; // invalid
    expect(component.isValidRegex()).toBe(false);
  });

  it('onTagsChange updates propertyTags', () => {
    component.propertyType.propertyTags = [] as any;
    const newTags = [{ name: 'tag1', type: 'LOCAL' }];
    component.onTagsChange(newTags);
    expect(component.propertyType.propertyTags).toEqual(newTags);
  });

  it('save emits property type and closes modal', () => {
    vi.spyOn(component.savePropertyType, 'emit');
    vi.spyOn(activeModal, 'close');
    component.propertyType = {
      id: null,
      name: 'NAME',
      validationRegex: '.*',
      encrypted: false,
      propertyTags: [],
    } as any;
    component.save();
    expect(component.savePropertyType.emit).toHaveBeenCalled();
    expect(activeModal.close).toHaveBeenCalled();
  });
});
