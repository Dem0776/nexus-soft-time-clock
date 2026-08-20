export type WorkSiteStatus = 'ACTIVE' | 'INACTIVE';

/**
 * Overrides de política del centro. `null` significa **heredar** la política de la empresa
 * (`company_settings`); `true`/`false` la sobrescriben para este centro. No confundir `null`
 * con `false`: enviar `false` desactiva la exigencia aunque la empresa la tenga activada.
 */
export type PolicyOverride = boolean | null;

export interface WorkSite {
  id: string;
  code: string;
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  timezone?: string;
  gpsAccuracyMaxM?: number;
  requirePhoto?: PolicyOverride;
  requireBiometric?: PolicyOverride;
  status: WorkSiteStatus;
}

export interface CreateWorkSite {
  code: string;
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  timezone?: string;
  gpsAccuracyMaxM?: number;
  requirePhoto?: PolicyOverride;
  requireBiometric?: PolicyOverride;
}

export interface UpdateWorkSite {
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  timezone?: string;
  gpsAccuracyMaxM?: number;
  requirePhoto?: PolicyOverride;
  requireBiometric?: PolicyOverride;
}
