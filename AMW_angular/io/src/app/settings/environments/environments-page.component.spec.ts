import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnvironmentsPageComponent } from './environments-page.component';

describe('EnvironmentsPageComponent', () => {
  let component: EnvironmentsPageComponent;
  let fixture: ComponentFixture<EnvironmentsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvironmentsPageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvironmentsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
