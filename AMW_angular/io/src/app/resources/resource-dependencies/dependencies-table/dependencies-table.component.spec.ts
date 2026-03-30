import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DependenciesTableComponent } from './dependencies-table.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('DependenciesTableComponent', () => {
  let component: DependenciesTableComponent;
  let fixture: ComponentFixture<DependenciesTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DependenciesTableComponent, RouterTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(DependenciesTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title', () => {
    component.title = 'Test Dependencies';
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Test Dependencies');
  });

  it('should render dependencies table', () => {
    component.dependencies = [
      {
        resourceId: 1,
        resourceName: 'TestResource',
        resourceTypeName: 'APPLICATION',
        releaseName: 'v1.0',
      },
    ];
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('tbody tr')).toBeTruthy();
    expect(compiled.textContent).toContain('TestResource');
  });
});
