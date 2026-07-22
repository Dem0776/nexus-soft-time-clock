/** Entrada de la bitácora de auditoría (RF-12), inmutable/append-only (RN-61). */
export interface AuditEntry {
  id: string;
  actorUserId: string;
  action: string;
  resourceType: string;
  resourceId: string;
  newValues?: string;
  createdAt: string;
}
