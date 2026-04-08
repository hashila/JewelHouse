import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ItemModel } from './item.model';

@Component({
  selector: 'app-view-item-dialog',
  imports: [CommonModule],
  templateUrl: './view-item-dialog.component.html'
})
export class ViewItemDialogComponent {
  @Input({ required: true }) item!: ItemModel;
  @Output() close = new EventEmitter<void>();

  constructor(private readonly sanitizer: DomSanitizer) {}

  get imageDataUrl(): SafeUrl | null {
    if (!this.item.image) {
      return null;
    }

    const base64 = this.toBase64(this.item.image);
    if (!base64) {
      return null;
    }

    const bytes = this.base64ToBytes(base64);
    const dataUrl = `data:${this.detectMimeType(bytes)};base64,${base64}`;
    return this.sanitizer.bypassSecurityTrustUrl(dataUrl);
  }

  private detectMimeType(bytes: Uint8Array): string {
    if (bytes.length >= 8 &&
      bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47) {
      return 'image/png';
    }
    if (bytes.length >= 3 &&
      bytes[0] === 0xff && bytes[1] === 0xd8 && bytes[2] === 0xff) {
      return 'image/jpeg';
    }
    if (bytes.length >= 4 &&
      bytes[0] === 0x47 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x38) {
      return 'image/gif';
    }
    if (bytes.length >= 12 &&
      bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x46 &&
      bytes[8] === 0x57 && bytes[9] === 0x45 && bytes[10] === 0x42 && bytes[11] === 0x50) {
      return 'image/webp';
    }
    return 'image/jpeg';
  }

  private toBase64(image: ItemModel['image']): string | null {
    if (!image) {
      return null;
    }

    if (typeof image === 'string') {
      const extracted = image.startsWith('data:') ? image.split(',')[1] || null : image;
      return extracted ? extracted.replace(/\s/g, '') : null;
    }

    if (!image.length) {
      return null;
    }

    const binary = Array.from(new Uint8Array(image), (byte) => String.fromCharCode(byte)).join('');
    return btoa(binary);
  }

  private base64ToBytes(base64: string): Uint8Array {
    try {
      const binary = atob(base64);
      return Uint8Array.from(binary, (char) => char.charCodeAt(0));
    } catch {
      return new Uint8Array();
    }
  }
}
