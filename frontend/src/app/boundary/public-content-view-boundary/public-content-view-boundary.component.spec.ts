import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicContentViewBoundaryComponent } from './public-content-view-boundary.component';

describe('PublicContentViewBoundaryComponent', () => {
  let component: PublicContentViewBoundaryComponent;
  let fixture: ComponentFixture<PublicContentViewBoundaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PublicContentViewBoundaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PublicContentViewBoundaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
