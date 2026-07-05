import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupManagementBoundaryComponent } from './group-management-boundary.component';

describe('GroupManagementBoundaryComponent', () => {
  let component: GroupManagementBoundaryComponent;
  let fixture: ComponentFixture<GroupManagementBoundaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GroupManagementBoundaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupManagementBoundaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
