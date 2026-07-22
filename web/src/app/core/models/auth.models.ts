/** Modelos de autenticación (espejo de los DTOs del backend). */

export interface LoginRequest {
  email: string;
  password: string;
  companyCode?: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface Me {
  userId: string;
  tenantId: string | null;
  platformAdmin: boolean;
  roles: string[];
  permissions: string[];
}
