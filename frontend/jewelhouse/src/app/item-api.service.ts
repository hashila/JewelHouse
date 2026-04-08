import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { ItemInput } from './item-form.utils';
import { ItemModel } from './item.model';
import {
  ActiveItemsQuery,
  ItemRequestDto,
  ItemResponseDto,
  ItemTaxResponseDto,
  PageResponse
} from './item-api.models';
import { ItemTaxLine } from './item.model';

@Injectable({ providedIn: 'root' })
export class ItemApiService {
  private readonly baseUrl = 'http://localhost:8080/api/items';

  constructor(private readonly http: HttpClient) {}

  getActiveItems(query: ActiveItemsQuery): Observable<PageResponse<ItemModel>> {
    let params = new HttpParams().set('page', query.page).set('pageSize', query.pageSize);
    params = params.set('sortBy', query.sortBy).set('sortDir', query.sortDir);
    const name = query.name?.trim();
    if (name) {
      params = params.set('name', name);
    }
    const metal = query.metalType?.trim();
    if (metal) {
      params = params.set('metalType', metal);
    }
    if (query.availability && /^[SOL]$/.test(query.availability)) {
      params = params.set('availability', query.availability);
    }
    return this.http
      .get<PageResponse<ItemResponseDto>>(`${this.baseUrl}/getActiveList`, { params })
      .pipe(map((response) => ({ ...response, content: response.content.map((item) => this.toUiModel(item)) })));
  }

  createItem(input: ItemInput): Observable<ItemModel> {
    return this.http
      .post<ItemResponseDto>(`${this.baseUrl}/create`, this.toRequestDto(input))
      .pipe(map((response) => this.toUiModel(response)));
  }

  getItemById(id: number): Observable<ItemModel> {
    const params = new HttpParams().set('id', id);
    return this.http
      .get<ItemResponseDto>(`${this.baseUrl}/getItemById`, { params })
      .pipe(map((response) => this.toUiModel(response)));
  }

  updateItem(id: number, input: ItemInput): Observable<ItemModel> {
    const params = new HttpParams().set('id', id);
    return this.http
      .put<ItemResponseDto>(`${this.baseUrl}/updateItem`, this.toRequestDto(input), { params })
      .pipe(map((response) => this.toUiModel(response)));
  }

  deleteItem(id: number): Observable<void> {
    const params = new HttpParams().set('id', id);
    return this.http.delete<void>(`${this.baseUrl}/delete`, { params });
  }

  private toUiModel(dto: ItemResponseDto): ItemModel {
    return {
      id: dto.id,
      name: dto.name,
      metalType: dto.metalType,
      weight: String(dto.weight),
      makingCharges: String(dto.makingCharges),
      taxes: this.taxesFromDto(dto.taxes),
      availability: this.availabilityCharToLabel(dto.availability),
      image: dto.image
    };
  }

  private taxesFromDto(list: ItemTaxResponseDto[] | null | undefined): ItemTaxLine[] {
    if (!list?.length) {
      return [];
    }
    return list.map((t) => ({
      taxName: t.taxName ?? '',
      taxPercentage: t.taxPercentage != null ? String(t.taxPercentage) : ''
    }));
  }

  private toRequestDto(input: ItemInput): ItemRequestDto {
    const taxes = (input.taxes ?? [])
      .map((t) => ({
        taxName: String(t.taxName).trim(),
        taxPercentage: Number(String(t.taxPercentage).trim())
      }))
      .filter((t) => t.taxName.length > 0 && !Number.isNaN(t.taxPercentage) && t.taxPercentage >= 0 && t.taxPercentage <= 100);

    return {
      name: input.name,
      metalType: input.metalType,
      weight: this.toNumber(input.weight),
      makingCharges: this.toNumber(input.makingCharges),
      availability: this.availabilityLabelToChar(input.availability),
      status: 'A',
      image: this.normalizeImagePayload(input.image),
      taxes: taxes.length ? taxes : []
    };
  }

  private availabilityCharToLabel(value: string): ItemInput['availability'] {
    if (value === 'L') {
      return 'Limited';
    }
    if (value === 'O') {
      return 'Out of Stock';
    }
    return 'In Stock';
  }

  private availabilityLabelToChar(value: ItemInput['availability']): string {
    if (value === 'Limited') {
      return 'L';
    }
    if (value === 'Out of Stock') {
      return 'O';
    }
    return 'S';
  }

  private toNumber(value: string): number {
    return Number(value.replace(/[^0-9.]/g, '')) || 0;
  }

  private normalizeImagePayload(image: ItemInput['image']): string | null {
    if (!image) {
      return null;
    }
    if (typeof image === 'string') {
      return image;
    }
    const binary = Array.from(new Uint8Array(image), (byte) => String.fromCharCode(byte)).join('');
    return btoa(binary);
  }
}
