import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PropertyDeleteModalComponent } from './property-delete-modal.component';
import { PropertyDeleteModalService } from '../services/property-delete-modal.service';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { signal } from '@angular/core';

describe('PropertyDeleteModalComponent', () => {
  let component: PropertyDeleteModalComponent;
  let fixture: ComponentFixture<PropertyDeleteModalComponent>;
  let mockModalRef: NgbModalRef;
  let mockDeleteModalService: PropertyDeleteModalService;

  beforeEach(async () => {
    mockModalRef = {
      dismiss: vi.fn(),
      close: vi.fn(),
    } as any;

    mockDeleteModalService = {
      descriptorToDelete: signal({ id: 1, name: 'Test Property' }),
      isDeleting: signal(false),
    } as any;

    await TestBed.configureTestingModule({
      imports: [PropertyDeleteModalComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PropertyDeleteModalComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('deleteModalService', mockDeleteModalService);
    fixture.componentRef.setInput('modal', mockModalRef);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit confirmDelete when onConfirmDelete is called', () => {
    const emitSpy = vi.spyOn(component.confirmDelete, 'emit');
    component.onConfirmDelete();

    expect(emitSpy).toHaveBeenCalledWith(mockModalRef);
  });

  it('should dismiss modal when onDismiss is called', () => {
    component.onDismiss();

    expect(mockModalRef.dismiss).toHaveBeenCalled();
  });
});
