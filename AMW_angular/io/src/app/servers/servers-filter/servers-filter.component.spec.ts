import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ServersFilterComponent } from './servers-filter.component';
import { InputSignal, signal } from '@angular/core';
import { Environment } from '../../deployment/environment';
import { Resource } from '../../resources/models/resource';

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
    component.environments = signal<Environment[]>([]) as unknown as InputSignal<Environment[]>;
    component.runtimes = signal<Resource[]>([]) as unknown as InputSignal<Resource[]>;
    component.appServerSuggestions = signal<string[]>([]) as unknown as InputSignal<string[]>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
