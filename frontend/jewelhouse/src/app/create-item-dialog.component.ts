import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AVAILABILITY_OPTIONS, EMPTY_ITEM_INPUT, ItemInput, sanitizeItemInput } from './item-form.utils';
import { MetalApiService } from './metal-api.service';
import { MetalResponseDto } from './metal-api.models';

@Component({
  selector: 'app-create-item-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './create-item-dialog.component.html'
})
export class CreateItemDialogComponent implements OnInit {
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<ItemInput>();

  readonly availabilityOptions = AVAILABILITY_OPTIONS;
  model: ItemInput = { ...EMPTY_ITEM_INPUT };
  metals: MetalResponseDto[] = [];

  constructor(private readonly metalApi: MetalApiService) {}

  ngOnInit(): void {
    this.metalApi.getList().subscribe({
      next: (list) => {
        this.metals = list ?? [];
      },
      error: () => {
        this.metals = [];
      }
    });
  }

  addTaxRow(): void {
    const next = [...(this.model.taxes ?? [])];
    next.push({ taxName: '', taxPercentage: '' });
    this.model.taxes = next;
  }

  removeTaxRow(index: number): void {
    this.model.taxes = (this.model.taxes ?? []).filter((_, i) => i !== index);
  }

  async onImageChange(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      this.model.image = null;
      return;
    }

    this.model.image = await this.readFileAsBase64(file);
  }

  onSave(): void {
    const sanitizedModel = sanitizeItemInput(this.model);
    if (!sanitizedModel.name || !sanitizedModel.metalType) {
      return;
    }
    this.save.emit(sanitizedModel);
  }

  private readFileAsBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = String(reader.result || '');
        resolve(result.includes(',') ? result.split(',')[1] : result);
      };
      reader.onerror = () => reject(reader.error);
      reader.readAsDataURL(file);
    });
  }
}
