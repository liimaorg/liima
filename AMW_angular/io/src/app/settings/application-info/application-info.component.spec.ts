import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationInfoComponent } from './application-info.component';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';

describe('ApplicationInfoComponent', () => {
  let component: ApplicationInfoComponent;
  let fixture: ComponentFixture<ApplicationInfoComponent>;
  let httpTestingController: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationInfoComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    httpTestingController = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
