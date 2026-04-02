import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DependenciesTableComponent } from './dependencies-table.component';
import { provideRouter } from '@angular/router';

describe('DependenciesTableComponent', () => {
  let component: DependenciesTableComponent;
  let fixture: ComponentFixture<DependenciesTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DependenciesTableComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DependenciesTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title', () => {
    const newFixture = TestBed.createComponent(DependenciesTableComponent);
    const newComponent = newFixture.componentInstance;
    newComponent.title = 'Test Dependencies';
    newFixture.detectChanges();
    const compiled = newFixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h2')?.textContent).toContain('Test Dependencies');
  });

  it('should render dependencies table', () => {
    const testDependencies = [
      {
        resourceId: 1,
        resourceName: 'TestResource',
        resourceTypeName: 'APPLICATION',
        releaseName: 'v1.0',
      },
    ];
    
    const newFixture = TestBed.createComponent(DependenciesTableComponent);
    const newComponent = newFixture.componentInstance;
    newComponent.dependencies = testDependencies;
    newFixture.detectChanges();
    
    const compiled = newFixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('tbody tr')).toBeTruthy();
    expect(compiled.textContent).toContain('TestResource');
  });
});
