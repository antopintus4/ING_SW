import { Component, Inject, PLATFORM_ID, ElementRef, ViewChild } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';

declare var bootstrap: any;

@Component({
  selector: 'app-message-boundary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './message-boundary.component.html',
  styleUrl: './message-boundary.component.css'
})
export class MessageBoundaryComponent {
  @ViewChild('modalEl') modalEl!: ElementRef;
  title: string = '';
  message: string = '';
  type: 'confirm' | 'message' = 'message';
  confirmBtnLabel: string = 'Sì';
  cancelBtnLabel: string = 'No';
  
  private onConfirmCallback?: () => void;
  private onCancelCallback?: () => void;
  private onOkCallback?: () => void;
  private modalInstance: any;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  showConfirmMessage(message: string, onConfirm: () => void, onCancel?: () => void, confirmLabel: string = 'Sì', cancelLabel: string = 'No') {
    this.title = 'Conferma';
    this.message = message;
    this.type = 'confirm';
    this.onConfirmCallback = onConfirm;
    this.onCancelCallback = onCancel;
    this.confirmBtnLabel = confirmLabel;
    this.cancelBtnLabel = cancelLabel;
    this.openModal();
  }

  sendMessage(message: string, onOk?: () => void) {
    this.title = 'Messaggio';
    this.message = message;
    this.type = 'message';
    this.onOkCallback = onOk;
    this.openModal();
  }

  showMessage(message: string, onOk?: () => void) {
    this.sendMessage(message, onOk);
  }

  clickSi() {
    this.closeModal();
    if (this.onConfirmCallback) {
      this.onConfirmCallback();
    }
  }

  clickNo() {
    this.closeModal();
    if (this.onCancelCallback) {
      this.onCancelCallback();
    }
  }

  clickOk() {
    this.closeModal();
    if (this.onOkCallback) {
      this.onOkCallback();
    }
  }

  private openModal() {
    if (isPlatformBrowser(this.platformId)) {
      if (this.modalEl && this.modalEl.nativeElement) {
        this.modalInstance = new bootstrap.Modal(this.modalEl.nativeElement, {
          backdrop: 'static',
          keyboard: false
        });
        this.modalInstance.show();
      }
    }
  }

  private closeModal() {
    if (this.modalInstance) {
      this.modalInstance.hide();
    }
  }
}
