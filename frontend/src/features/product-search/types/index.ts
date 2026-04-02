export interface NormalizedProduct {
  source_type: string;
  source_name: string;
  external_product_id: string;
  title: string;
  description: string;
  category: string;
  brand: string;
  price: number;
  currency: string;
  availability: string;
  merchant_id: string | null;
  merchant_name: string;
  merchant_rating: number | null;
  shipping_cost: number | null;
  shipping_eta: string;
  image_urls: string[];
  product_url: string;
  attributes: Record<string, string>;
  last_synced_at: string | null;
}

export interface TrustScore {
  recommendation_confidence: number;
  constraint_satisfaction: number;
  substitution_risk: string;
  merchant_trust: string;
  actionability: string;
  overall_trust_score: number;
}

export interface Explanation {
  top_reasons: string[];
  tradeoffs: string[];
  budget_match: boolean;
  brand_match: string;
  merchant_trust: string;
}

export interface UncertaintyAssessment {
  confidence: number;
  is_uncertain: boolean;
  reasons: string[];
  recommendation: string;
}

export interface RankedProduct {
  product: NormalizedProduct;
  score: number;
  rank: number;
  explanation: Explanation;
  trust_score: TrustScore;
}

export interface SearchResponse {
  products: NormalizedProduct[];
  sourcesUsed: string[];
  totalFound: number;
  queryProcessedAt: string;
  confidence: number;
  strategyUsed: string;
  uncertainty: UncertaintyAssessment;
  rankedProducts: RankedProduct[];
}
