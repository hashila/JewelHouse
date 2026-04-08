import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ItemModel } from './item.model';
import { AVAILABILITY_OPTIONS, sanitizeItemInput } from './item-form.utils';
import { MetalApiService } from './metal-api.service';
import { MetalResponseDto } from './metal-api.models';

@Component({
  selector: 'app-update-item-dialog',
  imports: [CommonModule, FormsModule],
  templateUrl: './update-item-dialog.component.html'
})
export class UpdateItemDialogComponent implements OnChanges, OnInit {
  @Input({ required: true }) item!: ItemModel;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<ItemModel>();

  model!: ItemModel;
  readonly availabilityOptions = AVAILABILITY_OPTIONS;
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

  /** Includes current metal name if it is not in the API list (legacy data). */
  get metalSelectOptions(): MetalResponseDto[] {
    const list = [...this.metals];
    const cur = this.model?.metalType?.trim();
    if (cur && !list.some((m) => m.name === cur)) {
      list.unshift({ id: -1, name: cur });
    }
    return list;
  }

  async onImageChange(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.model.image = await this.readFileAsBase64(file);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['item'] && this.item) {
      this.model = {
        ...this.item,
        taxes: (this.item.taxes ?? []).map((t) => ({ ...t }))
      };
    }
  }

  addTaxRow(): void {
    const next = [...(this.model.taxes ?? [])];
    next.push({ taxName: '', taxPercentage: '' });
    this.model.taxes = next;
  }

  removeTaxRow(index: number): void {
    this.model.taxes = (this.model.taxes ?? []).filter((_, i) => i !== index);
  }

  onSave(): void {
    const sanitizedModel = sanitizeItemInput(this.model);
    if (!sanitizedModel.name || !sanitizedModel.metalType) {
      return;
    }
    this.save.emit({
      ...this.model,
      ...sanitizedModel
    });
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
