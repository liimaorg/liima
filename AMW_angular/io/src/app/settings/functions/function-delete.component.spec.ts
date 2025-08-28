import { TestBed, ComponentFixture } from '@angular/core/testing';
import { FunctionDeleteComponent } from './function-delete.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('FunctionDeleteComponent', () => {
  let fixture: ComponentFixture<FunctionDeleteComponent>;
  let component: FunctionDeleteComponent;
  let activeModal: NgbActiveModal;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FunctionDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();
    fixture = TestBed.createComponent(FunctionDeleteComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
    component.function = { id: 1, name: 'TestFunc' } as any;
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('calls activeModal.close on cancel()', () => {
    spyOn(activeModal, 'close');
    component.cancel();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('emits deleteFunction and closes on delete()', () => {
    const testFn: any = { id: 1 };
    component.function = testFn;
    spyOn(component.deleteFunction, 'emit');
    spyOn(activeModal, 'close');
    component.delete();
    expect(component.deleteFunction.emit).toHaveBeenCalledWith(testFn);
    expect(activeModal.close).toHaveBeenCalled();
  });
});
