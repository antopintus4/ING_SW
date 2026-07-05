import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupViewBoundaryComponent } from './group-view-boundary.component';

describe('GroupViewBoundaryComponent', () => {
  let component: GroupViewBoundaryComponent;
  let fixture: ComponentFixture<GroupViewBoundaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupViewBoundaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupViewBoundaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
