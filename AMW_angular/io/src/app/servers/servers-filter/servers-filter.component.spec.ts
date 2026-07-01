import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ServersFilterComponent } from './servers-filter.component';

describe('ServersFilterComponent', () => {
  let component: ServersFilterComponent;
  let fixture: ComponentFixture<ServersFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServersFilterComponent],
      providers: [],
    }).compileComponents();

    fixture = TestBed.createComponent(ServersFilterComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('environments', []);
    fixture.componentRef.setInput('runtimes', []);
    fixture.componentRef.setInput('appServerSuggestions', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
