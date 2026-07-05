// ============================================
// Common Types — Pagination, Error Response
// ============================================

export interface Paginated<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // current page (0-indexed from Spring)
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  message: string;
  error?: string;
}
