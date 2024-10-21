import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnvironmentEditComponent } from './environment-edit.component';

describe('EnvironmentEditComponent', () => {
  let component: EnvironmentEditComponent;
  let fixture: ComponentFixture<EnvironmentEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvironmentEditComponent],
      providers: [],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvironmentEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
