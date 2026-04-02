export const AUTH = {
  REGISTER: "/api/auth/register",
  LOGIN: "/api/auth/login",
} as const;

export const PROFILE = {
  ME: "/api/profile/me",
} as const;

export const PREFERENCES = {
  BASE: "/api/preferences",
} as const;

export const DISCOVERY = {
  SEARCH: "/api/discovery/search",
} as const;

export const CART = {
  BASE: "/api/cart",
  ITEMS: "/api/cart/items",
  CHECKOUT: "/api/cart/checkout",
} as const;
