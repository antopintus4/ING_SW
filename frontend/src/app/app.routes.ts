import { Routes } from '@angular/router';

import { HomeBoundaryComponent } from './boundary/home-boundary/home-boundary.component';
import { LoginBoundaryComponent } from './boundary/login-boundary/login-boundary.component';
import { RegistrationBoundaryComponent } from './boundary/registration-boundary/registration-boundary.component';
import { ProfileBoundaryComponent } from './boundary/profile-boundary/profile-boundary.component';
import { ContentManagementBoundaryComponent } from './boundary/content-management-boundary/content-management-boundary.component';
import { UploadContentBoundaryComponent } from './boundary/upload-content-boundary/upload-content-boundary.component';
import { ContentViewBoundaryComponent } from './boundary/content-view-boundary/content-view-boundary.component';
import { ShareBoundaryComponent } from './boundary/share-boundary/share-boundary.component';
import { ActiveLinksBoundaryComponent } from './boundary/active-links-boundary/active-links-boundary.component';
import { LinkSettingsBoundaryComponent } from './boundary/link-settings-boundary/link-settings-boundary.component';
import { ResultsBoundaryComponent } from './boundary/results-boundary/results-boundary.component';
import { MessageBoundaryComponent } from './boundary/message-boundary/message-boundary.component';
import { ErrorPageBoundaryComponent } from './boundary/error-page-boundary/error-page-boundary.component';
import { ErrorMessageBoundaryComponent } from './boundary/error-message-boundary/error-message-boundary.component';
import { OtpBoundaryComponent } from './boundary/otp-boundary/otp-boundary.component';
import { RecoveryBoundaryComponent } from './boundary/recovery-boundary/recovery-boundary.component';
import { RecoveryResetBoundaryComponent } from './boundary/recovery-reset-boundary/recovery-reset-boundary.component';
import { EditCredentialsBoundaryComponent } from './boundary/edit-credentials-boundary/edit-credentials-boundary.component';
import { EditPasswordBoundaryComponent } from './boundary/edit-password-boundary/edit-password-boundary.component';
import { EditAnagraficaBoundaryComponent } from './boundary/edit-anagrafica-boundary/edit-anagrafica-boundary.component';
import { OtherEditsBoundaryComponent } from './boundary/other-edits-boundary/other-edits-boundary.component';
import { PreviewBoundaryComponent } from './boundary/preview-boundary/preview-boundary.component';
import { IdentityProviderBoundaryComponent } from './boundary/identity-provider-boundary/identity-provider-boundary.component';
import { GroupManagementBoundaryComponent } from './boundary/group-management-boundary/group-management-boundary.component';
import { GroupViewBoundaryComponent } from './boundary/group-view-boundary/group-view-boundary.component';
import { PublicContentViewBoundaryComponent } from './boundary/public-content-view-boundary/public-content-view-boundary.component';
import { PublicProfileBoundaryComponent } from './boundary/public-profile-boundary/public-profile-boundary.component';
import { EditContentBoundaryComponent } from './boundary/edit-content-boundary/edit-content-boundary.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', component: HomeBoundaryComponent },
    { path: 'login', component: LoginBoundaryComponent },
    { path: 'register', component: RegistrationBoundaryComponent },
    { path: 'otp', component: OtpBoundaryComponent },
    { path: 'recovery', component: RecoveryBoundaryComponent },
    { path: 'recovery/reset', component: RecoveryResetBoundaryComponent },
    { path: 'idp', component: IdentityProviderBoundaryComponent },
    { path: 'profile', component: ProfileBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'profile/edit-credentials', component: EditCredentialsBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'profile/edit-password', component: EditPasswordBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'profile/edit-anagrafica', component: EditAnagraficaBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'profile/edit-other', component: OtherEditsBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'content', component: ContentManagementBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'content/upload', component: UploadContentBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'content/view/:id', component: ContentViewBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'content/edit/:id', component: EditContentBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'content/preview', component: PreviewBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'public/content/:id', component: PublicContentViewBoundaryComponent },
    { path: 'public/profile/:id', component: PublicProfileBoundaryComponent },
    { path: 'share/:identificatore', component: ShareBoundaryComponent },
    { path: 'search', component: ResultsBoundaryComponent },
    { path: 'groups', component: GroupManagementBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'groups/view/:id', component: GroupViewBoundaryComponent, canActivate: [AuthGuard] },
    { path: 'message', component: MessageBoundaryComponent },
    { path: 'error', component: ErrorPageBoundaryComponent },
    { path: 'error-msg', component: ErrorMessageBoundaryComponent },
    { path: '**', redirectTo: '/error' }
];
