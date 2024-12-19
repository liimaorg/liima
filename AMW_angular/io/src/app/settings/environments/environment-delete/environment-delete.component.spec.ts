import { EnvironmentDeleteComponent } from './environment-delete.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ComponentFixture, TestBed } from '@angular/core/testing';

describe('EnvironmentDeleteComponent', () => {
  let component: EnvironmentDeleteComponent;
  let fixture: ComponentFixture<EnvironmentDeleteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvironmentDeleteComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvironmentDeleteComponent);
    component = fixture.componentInstance;

    component.environment = {
      id: 2,
      name: 'Dev',
      nameAlias: null,
      parentName: 'Global',
      parentId: 1,
      selected: undefined,
      disabled: undefined,
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
