import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreateItemDialogComponent } from './create-item-dialog.component';
import { ItemApiService } from './item-api.service';
import { ActiveItemsQuery, ActiveItemsSortOption, PageResponse, toActiveItemsSortParams } from './item-api.models';
import { ItemModel } from './item.model';
import { AVAILABILITY_OPTIONS, availabilityLabelToApiFilter, ItemInput } from './item-form.utils';
import { MetalApiService } from './metal-api.service';
import { MetalResponseDto } from './metal-api.models';
import { UpdateItemDialogComponent } from './update-item-dialog.component';
import { ViewItemDialogComponent } from './view-item-dialog.component';

@Component({
  selector: 'app-item-management',
  imports: [CommonModule, FormsModule, CreateItemDialogComponent, UpdateItemDialogComponent, ViewItemDialogComponent],
  templateUrl: './item-management.component.html'
})
export class ItemManagementComponent implements OnDestroy {
  private readonly allOption = 'All';
  managedItems: ItemModel[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  isLoading = false;
  errorMessage = '';

  selectedActionRowId: number | null = null;
  createDialogOpen = false;
  viewDialogItem: ItemModel | null = null;
  updateDialogItem: ItemModel | null = null;
  searchTerm = '';
  selectedMetalType = this.allOption;
  selectedAvailability = this.allOption;
  sortBy: ActiveItemsSortOption = 'name-asc';
  /** From GET /api/metals/getList — metal filter dropdown. */
  metalCatalog: MetalResponseDto[] = [];

  private searchDebounceTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private readonly itemApi: ItemApiService,
    private readonly metalApi: MetalApiService
  ) {
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

  get availabilities(): string[] {
    return [this.allOption, ...AVAILABILITY_OPTIONS];
  }

  get canGoPrevious(): boolean {
    return this.currentPage > 0 && !this.isLoading;
  }

  get canGoNext(): boolean {
    return this.currentPage < this.totalPages - 1 && !this.isLoading;
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

  toggleActions(itemId: number): void {
    this.selectedActionRowId = this.selectedActionRowId === itemId ? null : itemId;
  }

  openCreateDialog(): void {
    this.createDialogOpen = true;
  }

  openViewDialog(item: ItemModel): void {
    this.selectedActionRowId = null;
    this.itemApi.getItemById(item.id).subscribe({
      next: (response) => {
        this.viewDialogItem = response;
      },
      error: () => {
        this.errorMessage = 'Failed to load item details.';
      }
    });
  }

  openUpdateDialog(item: ItemModel): void {
    this.selectedActionRowId = null;
    this.itemApi.getItemById(item.id).subscribe({
      next: (response) => {
        this.updateDialogItem = response;
      },
      error: () => {
        this.errorMessage = 'Failed to load item details for update.';
      }
    });
  }

  deleteItem(itemId: number): void {
    this.itemApi.deleteItem(itemId).subscribe({
      next: () => {
        this.loadItems(this.currentPage);
      },
      error: () => {
        this.errorMessage = 'Failed to delete item.';
      }
    });
    this.selectedActionRowId = null;
  }

  createItem(item: ItemInput): void {
    this.itemApi.createItem(item).subscribe({
      next: () => {
        this.createDialogOpen = false;
        this.loadItems(0);
      },
      error: () => {
        this.errorMessage = 'Failed to create item.';
      }
    });
  }

  updateItem(updatedItem: ItemModel): void {
    this.itemApi.updateItem(updatedItem.id, updatedItem).subscribe({
      next: () => {
        this.updateDialogItem = null;
        this.loadItems(this.currentPage);
      },
      error: () => {
        this.errorMessage = 'Failed to update item.';
      }
    });
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

  formatWeight(value: string): string {
    return `${value}g`;
  }

  formatMakingCharges(value: string): string {
    return `$${value}`;
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

  private loadItems(page: number = 0): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.itemApi.getActiveItems(this.buildQuery(page)).subscribe({
      next: (response) => {
        this.applyPageResponse(response);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load items from backend.';
        this.isLoading = false;
      }
    });
  }

  private applyPageResponse(response: PageResponse<ItemModel>): void {
    this.managedItems = response.content;
    this.currentPage = response.number;
    this.pageSize = response.size;
    this.totalElements = response.totalElements;
    this.totalPages = response.totalPages;
  }
}
