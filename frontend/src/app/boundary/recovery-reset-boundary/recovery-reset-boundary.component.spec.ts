import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecoveryResetBoundaryComponent } from './recovery-reset-boundary.component';

describe('RecoveryResetBoundaryComponent', () => {
  let component: RecoveryResetBoundaryComponent;
  let fixture: ComponentFixture<RecoveryResetBoundaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecoveryResetBoundaryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecoveryResetBoundaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
