import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PublicProfileBoundaryComponent } from './public-profile-boundary.component';

describe('PublicProfileBoundaryComponent', () => {
  let component: PublicProfileBoundaryComponent;
  let fixture: ComponentFixture<PublicProfileBoundaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PublicProfileBoundaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PublicProfileBoundaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
