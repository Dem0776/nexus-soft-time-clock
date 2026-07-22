/** Envelope de paginación (espejo de PageResponse del backend). */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
