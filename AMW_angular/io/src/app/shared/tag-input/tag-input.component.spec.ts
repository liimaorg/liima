import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TagInputComponent } from './tag-input.component';

describe('TagInputComponent', () => {
  let component: TagInputComponent;
  let fixture: ComponentFixture<TagInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TagInputComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TagInputComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('tags', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add a new tag when addTag is called with valid input', () => {
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('newTag');
    component.addTag();

    expect(emitSpy).toHaveBeenCalledWith([{ name: 'newTag', type: 'LOCAL' }]);
    expect(component.newTagInput()).toBe('');
  });

  it('should trim whitespace when adding a tag', () => {
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('  tagWithSpaces  ');
    component.addTag();

    expect(emitSpy).toHaveBeenCalledWith([{ name: 'tagWithSpaces', type: 'LOCAL' }]);
  });

  it('should not add duplicate tags', () => {
    fixture.componentRef.setInput('tags', [{ name: 'existing', type: 'LOCAL' }]);
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('existing');
    component.addTag();

    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('should not add empty tags', () => {
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('   ');
    component.addTag();

    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('should remove a tag when removeTag is called', () => {
    fixture.componentRef.setInput('tags', [
      { name: 'tag1', type: 'LOCAL' },
      { name: 'tag2', type: 'LOCAL' },
    ]);
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.removeTag('tag1');

    expect(emitSpy).toHaveBeenCalledWith([{ name: 'tag2', type: 'LOCAL' }]);
  });

  it('should add tag on Enter key press', () => {
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('enterTag');
    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    vi.spyOn(event, 'preventDefault');
    component.onTagInputKeydown(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(emitSpy).toHaveBeenCalledWith([{ name: 'enterTag', type: 'LOCAL' }]);
  });

  it('should not add tag on other key press', () => {
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('someTag');
    const event = new KeyboardEvent('keydown', { key: 'a' });
    component.onTagInputKeydown(event);

    expect(emitSpy).not.toHaveBeenCalled();
  });

  it('should use custom tagType when provided', () => {
    fixture.componentRef.setInput('tagType', 'CUSTOM');
    const emitSpy = vi.spyOn(component.tagsChange, 'emit');
    component.newTagInput.set('customTag');
    component.addTag();

    expect(emitSpy).toHaveBeenCalledWith([{ name: 'customTag', type: 'CUSTOM' }]);
  });
});
