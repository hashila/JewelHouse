export interface ItemTaxLine {
  taxName: string;
  taxPercentage: string;
}

export interface ItemModel {
  id: number;
  name: string;
  metalType: string;
  weight: string;
  makingCharges: string;
  shippingCharges: string;
  price?: string;
  taxes: ItemTaxLine[];
  availability: string;
  image: string | number[] | null;
}
