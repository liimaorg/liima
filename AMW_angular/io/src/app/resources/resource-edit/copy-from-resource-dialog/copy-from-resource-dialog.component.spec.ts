import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CopyFromResourceDialogComponent } from './copy-from-resource-dialog.component';

describe('CopyFromResourceDialogComponent', () => {
  let component: CopyFromResourceDialogComponent;
  let fixture: ComponentFixture<CopyFromResourceDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CopyFromResourceDialogComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(CopyFromResourceDialogComponent);
    component = fixture.componentInstance;
    component.resourceId = 1;
    component.resourceTypeName = 'APPLICATION';
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
