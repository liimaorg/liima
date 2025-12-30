import { ComponentRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaginationComponent } from './pagination.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

describe('PaginationComponent', () => {
  let component: PaginationComponent;
  let componentRef: ComponentRef<PaginationComponent>;
  let fixture: ComponentFixture<PaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationComponent, CommonModule],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PaginationComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', 3);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should return no page numbers if there is just one page', () => {
    // given
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', 1);

    // when
    const pages = component.pages();

    // then
    expect(pages).toBeUndefined();
  });

  it('should return two page numbers if last page is 2', () => {
    // given
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', 2);

    // when
    const pages = component.pages();

    // then
    expect(pages.length).toEqual(2);
    expect(pages).toEqual([1, 2]);
  });

  it('should not return the right pages', () => {
    // given
    component.paginatorItems = 5;

    componentRef.setInput('currentPage', 5);
    componentRef.setInput('lastPage', component.paginatorItems + 10);

    // when
    const pages = component.pages();

    // then
    expect(pages.length).toEqual(component.paginatorItems);
    expect(pages).toEqual([3, 4, 5, 6, 7]);
  });

  it('should emit the right offset on toPage', () => {
    // given
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', component.paginatorItems + 10);

    vi.spyOn(component.doSetOffset, 'emit');

    // when
    component.toPage(2);

    // then
    expect(component.doSetOffset.emit).toHaveBeenCalledWith(10);
  });

  it('should emit the right results offset when navigating to first page', () => {
    // given
    componentRef.setInput('currentPage', 2);
    componentRef.setInput('lastPage', 10);

    vi.spyOn(component.doSetOffset, 'emit');

    // when
    component.toPage(1);

    // then
    expect(component.doSetOffset.emit).toHaveBeenCalledWith(0);
  });

  it('should emit the right results offset when navigating to second page', () => {
    // given
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', 10);

    vi.spyOn(component.doSetOffset, 'emit');

    // when
    component.toPage(2);

    // then
    expect(component.doSetOffset.emit).toHaveBeenCalledWith(10);
  });

  it('should emit the right results offset depending on maxResults when navigating to second page', () => {
    // given
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', 10);

    component.maxResults = 50;
    vi.spyOn(component.doSetOffset, 'emit');

    // when
    component.toPage(2);

    // then
    expect(component.doSetOffset.emit).toHaveBeenCalledWith(50);
  });

  it('should emit the right results offset when navigating to last page', () => {
    // given
    componentRef.setInput('currentPage', 1);
    componentRef.setInput('lastPage', 10);

    vi.spyOn(component.doSetOffset, 'emit');

    // when
    component.toPage(10);

    // then
    expect(component.doSetOffset.emit).toHaveBeenCalledWith(90);
  });
});
