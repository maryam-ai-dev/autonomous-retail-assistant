"""Pydantic model for normalised products matching normalized_product.json contract."""

from datetime import datetime
from typing import Dict, List, Optional

from pydantic import BaseModel


class NormalizedProduct(BaseModel):
    source_type: str
    source_name: str
    external_product_id: Optional[str] = None
    title: str
    description: Optional[str] = ""
    category: Optional[str] = ""
    brand: Optional[str] = "Unknown"
    price: float
    currency: Optional[str] = "USD"
    availability: Optional[str] = ""
    merchant_id: Optional[str] = None
    merchant_name: str
    merchant_rating: Optional[float] = None
    shipping_cost: Optional[float] = 0.0
    shipping_eta: Optional[str] = ""
    image_urls: Optional[List[str]] = []
    product_url: Optional[str] = ""
    attributes: Optional[Dict[str, str]] = {}
    last_synced_at: Optional[datetime] = None
