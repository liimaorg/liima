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
    spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('isValidRegex handles empty regex', () => {
    component.propertyType.validationRegex = '';
    expect(component.isValidRegex()).toBeTrue();
  });

  it('isValidRegex returns false on invalid regex', () => {
    component.propertyType.validationRegex = '['; // invalid
    expect(component.isValidRegex()).toBeFalse();
  });

  it('addTag trims and adds tag then resets input', () => {
    component.propertyType.propertyTags = [] as any;
    component.newTag = '  tag1  ';
    component.addTag();
    expect(component.propertyType.propertyTags.length).toBe(1);
    expect(component.newTag).toBe('');
  });

  it('deleteTag removes matching tag', () => {
    component.propertyType.propertyTags = [
      { name: 'a', type: 'LOCAL' },
      { name: 'b', type: 'LOCAL' },
    ] as any;
    component.deleteTag({ name: 'a', type: 'LOCAL' });
    expect(component.propertyType.propertyTags.map((t) => t.name)).toEqual(['b']);
  });

  it('save emits property type and closes modal', () => {
    spyOn(component.savePropertyType, 'emit');
    spyOn(activeModal, 'close');
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
