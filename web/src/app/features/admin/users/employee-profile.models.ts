/** Género del colaborador (espejo del enum Gender del backend). */
export type Gender = 'FEMALE' | 'MALE' | 'OTHER' | 'UNDISCLOSED';

export const GENDERS: { value: Gender; label: string }[] = [
  { value: 'FEMALE', label: 'Femenino' },
  { value: 'MALE', label: 'Masculino' },
  { value: 'OTHER', label: 'Otro' },
  { value: 'UNDISCLOSED', label: 'Prefiere no decir' },
];

/**
 * Perfil de empleado: datos personales OPCIONALES (1‑a‑1 con el usuario).
 * Autocontenido (incluye teléfono) para no acoplarse al módulo identity/users.
 */
export interface EmployeeProfile {
  userId?: string;
  birthDate?: string; // yyyy-MM-dd
  hireDate?: string; // yyyy-MM-dd
  gender?: Gender | null;
  phone?: string;
  address?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
}
