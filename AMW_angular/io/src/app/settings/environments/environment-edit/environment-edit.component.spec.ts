import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnvironmentEditComponent } from './environment-edit.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('EnvironmentEditComponent', () => {
  let component: EnvironmentEditComponent;
  let fixture: ComponentFixture<EnvironmentEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvironmentEditComponent],
      providers: [NgbActiveModal],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvironmentEditComponent);
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
