import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ItemApiService } from './item-api.service';
import { ItemModel } from './item.model';
import { MetalApiService } from './metal-api.service';
import { MetalResponseDto } from './metal-api.models';
import { ActiveItemsQuery, ActiveItemsSortOption, toActiveItemsSortParams } from './item-api.models';
import { availabilityLabelToApiFilter } from './item-form.utils';

type ViewMode = 'grid' | 'list';

interface JewelleryItem {
  id: number;
  name: string;
  metalType: string;
  category: string;
  weight: string;
  availability: string;
  price: number;
  image: string;
}

@Component({
  selector: 'app-home',
  imports: [FormsModule],
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly allOption = 'All';
  jewelleryItems: JewelleryItem[] = [];
  /** From GET /api/metals/getList — used for metal type filter options. */
  metalCatalog: MetalResponseDto[] = [];
  readonly fallbackImage = 'https://images.unsplash.com/photo-1617038220319-276d3cfab638?auto=format&fit=crop&w=800&q=80';
  readonly pageSize = 10;
  isLoading = false;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;

  viewMode: ViewMode = 'grid';
  searchTerm = '';
  selectedMetalType = this.allOption;
  selectedAvailability = this.allOption;
  sortBy: ActiveItemsSortOption = 'name-asc';

  private searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly itemApi: ItemApiService,
    private readonly metalApi: MetalApiService
  ) {}

  ngOnInit(): void {
    this.loadItems(0);
    this.loadMetalCatalog();
  }

  ngOnDestroy(): void {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }
  }

  get metalFilterOptions(): string[] {
    const names = this.metalCatalog.map((m) => m.name).filter(Boolean);
    return [this.allOption, ...names];
  }

  get availabilityFilterOptions(): string[] {
    return [this.allOption, ...['In Stock', 'Limited', 'Out of Stock']];
  }

  get canGoPrevious(): boolean {
    return this.currentPage > 0 && !this.isLoading;
  }

  get canGoNext(): boolean {
    return this.currentPage < this.totalPages - 1 && !this.isLoading;
  }

  formatPrice(value: number): string {
    return `$${value.toLocaleString()}`;
  }

  onSearchChange(): void {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer);
    }
    this.searchDebounceTimer = setTimeout(() => {
      this.searchDebounceTimer = null;
      this.loadItems(0);
    }, 400);
  }

  onFilterChange(): void {
    this.loadItems(0);
  }

  goToPreviousPage(): void {
    if (this.canGoPrevious) {
      this.loadItems(this.currentPage - 1);
    }
  }

  goToNextPage(): void {
    if (this.canGoNext) {
      this.loadItems(this.currentPage + 1);
    }
  }

  private loadMetalCatalog(): void {
    this.metalApi.getList().subscribe({
      next: (list) => {
        this.metalCatalog = list ?? [];
      },
      error: () => {
        this.metalCatalog = [];
      }
    });
  }

  private buildQuery(page: number): ActiveItemsQuery {
    const { sortBy, sortDir } = toActiveItemsSortParams(this.sortBy);
    return {
      page,
      pageSize: this.pageSize,
      name: this.searchTerm.trim() || undefined,
      metalType: this.selectedMetalType === this.allOption ? undefined : this.selectedMetalType,
      availability: availabilityLabelToApiFilter(this.selectedAvailability, this.allOption),
      sortBy,
      sortDir
    };
  }

  private loadItems(page: number): void {
    this.isLoading = true;
    this.itemApi.getActiveItems(this.buildQuery(page)).subscribe({
      next: (response) => {
        this.jewelleryItems = response.content.map((item) => this.toJewelleryItem(item));
        this.currentPage = response.number;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.jewelleryItems = [];
        this.isLoading = false;
      }
    });
  }

  private toJewelleryItem(item: ItemModel): JewelleryItem {
    return {
      id: item.id,
      name: item.name,
      metalType: item.metalType,
      category: 'Jewellery',
      weight: `${item.weight}g`,
      availability: item.availability,
      price: Number(item.makingCharges) || 0,
      image: this.toImageUrl(item.image) || this.fallbackImage
    };
  }

  private toImageUrl(image: ItemModel['image']): string | null {
    if (!image) {
      return null;
    }

    const base64 = typeof image === 'string' ? image.replace(/\s/g, '') : this.bytesToBase64(image);
    if (!base64) {
      return null;
    }

    const bytes = this.base64ToBytes(base64);
    return `data:${this.detectMimeType(bytes)};base64,${base64}`;
  }

  private bytesToBase64(image: number[]): string {
    if (!image.length) {
      return '';
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
}
