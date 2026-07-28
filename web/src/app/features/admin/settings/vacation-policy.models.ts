/** Política de vacaciones de la empresa (espejo de VacationPolicyResponse del backend). */
export interface VacationPolicy {
  daysPerYear: number;
  requireApproval: boolean;
  countBusinessDaysOnly: boolean;
}
