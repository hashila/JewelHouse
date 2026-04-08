import { ItemModel, ItemTaxLine } from './item.model';

export type ItemInput = Omit<ItemModel, 'id'>;

export const AVAILABILITY_OPTIONS: ReadonlyArray<ItemInput['availability']> = [
  'In Stock',
  'Limited',
  'Out of Stock'
];

/** Maps filter dropdown label to GET /getActiveList `availability` (S, O, L). */
export function availabilityLabelToApiFilter(selected: string, allOption: string): string | undefined {
  if (!selected || selected === allOption) {
    return undefined;
  }
  if (selected === 'Limited') {
    return 'L';
  }
  if (selected === 'Out of Stock') {
    return 'O';
  }
  return 'S';
}

export const EMPTY_ITEM_INPUT: ItemInput = {
  name: '',
  metalType: '',
  weight: '',
  makingCharges: '',
  taxes: [],
  availability: 'In Stock',
  image: null
};

/** Rows with a name and a numeric percentage 0–100 (incomplete rows are dropped). */
export function sanitizeItemTaxes(taxes: ItemTaxLine[] | undefined): ItemTaxLine[] {
  if (!taxes?.length) {
    return [];
  }
  return taxes
    .map((t) => ({
      taxName: String(t.taxName).trim(),
      taxPercentage: String(t.taxPercentage).trim()
    }))
    .filter((t) => {
      if (!t.taxName) {
        return false;
      }
      const n = Number(t.taxPercentage);
      return !Number.isNaN(n) && n >= 0 && n <= 100;
    });
}

export function formatTaxesSummary(taxes: ItemTaxLine[] | undefined): string {
  const list = sanitizeItemTaxes(taxes);
  if (!list.length) {
    return '—';
  }
  return list.map((t) => `${t.taxName} (${t.taxPercentage}%)`).join(', ');
}

export function sanitizeItemInput(input: ItemInput): ItemInput {
  return {
    name: String(input.name).trim(),
    metalType: String(input.metalType).trim(),
    weight: String(input.weight).trim(),
    makingCharges: String(input.makingCharges).trim(),
    taxes: sanitizeItemTaxes(input.taxes),
    availability: input.availability,
    image: input.image
  };
}
