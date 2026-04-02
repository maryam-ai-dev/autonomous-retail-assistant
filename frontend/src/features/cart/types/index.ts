export interface CartItem {
  id: string;
  externalProductId: string;
  title: string;
  price: number;
  currency: string;
  merchantId: string | null;
  merchantName: string;
  merchantRating: number | null;
  sourceType: string;
  sourceName: string;
  productUrl: string;
  imageUrl: string;
  substitution: boolean;
  originalProductId: string | null;
  addedAt: string;
}

export interface Cart {
  id: string;
  userId: string;
  status: string;
  items: CartItem[];
  totalAmount: number;
}

export interface CheckoutResult {
  outcome: "CHECKED_OUT" | "APPROVAL_REQUIRED" | "BLOCKED";
  approvalId?: string;
  reasons?: string[];
  totalAmount?: number;
}
