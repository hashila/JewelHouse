export interface PageResponse<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/** Query for GET /api/items/getActiveList */
export interface ActiveItemsQuery {
  page: number;
  pageSize: number;
  /** LIKE filter; omit when empty */
  name?: string;
  /** Exact match; omit when empty / “All” */
  metalType?: string;
  /** S | O | L; omit when not filtering */
  availability?: string;
  sortBy: 'name' | 'makingCharges';
  sortDir: 'asc' | 'desc';
}

export type ActiveItemsSortOption = 'name-asc' | 'name-desc' | 'charges-asc' | 'charges-desc';

export function toActiveItemsSortParams(option: ActiveItemsSortOption): Pick<ActiveItemsQuery, 'sortBy' | 'sortDir'> {
  switch (option) {
    case 'name-desc':
      return { sortBy: 'name', sortDir: 'desc' };
    case 'charges-asc':
      return { sortBy: 'makingCharges', sortDir: 'asc' };
    case 'charges-desc':
      return { sortBy: 'makingCharges', sortDir: 'desc' };
    default:
      return { sortBy: 'name', sortDir: 'asc' };
  }
}

export interface ItemTaxRequestDto {
  itemId?: number;
  taxName: string;
  taxPercentage: number;
}

export interface ItemTaxResponseDto {
  itemId?: number;
  taxName: string;
  taxPercentage: number;
}

export interface ItemRequestDto {
  name: string;
  metalType: string;
  weight: number;
  makingCharges: number;
  availability: string;
  status: string;
  image: string | null;
  taxes?: ItemTaxRequestDto[] | null;
}

export interface ItemResponseDto {
  id: number;
  name: string;
  metalType: string;
  weight: number;
  makingCharges: number;
  availability: string;
  status: string;
  image: string | number[] | null;
  taxes?: ItemTaxResponseDto[] | null;
  createdAt: string;
  updatedAt: string;
}
