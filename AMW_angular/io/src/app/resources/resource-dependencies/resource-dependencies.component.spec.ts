import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResourceDependenciesComponent } from './resource-dependencies.component';
import { ResourceDependenciesService } from '../services/resource-dependencies.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('ResourceDependenciesComponent', () => {
  let component: ResourceDependenciesComponent;
  let fixture: ComponentFixture<ResourceDependenciesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourceDependenciesComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ResourceDependenciesService,
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of(
              new Map([
                ['id', '1'],
                ['ctx', '1'],
              ]),
            ),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourceDependenciesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
