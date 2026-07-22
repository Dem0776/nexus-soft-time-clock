export type WorkSiteStatus = 'ACTIVE' | 'INACTIVE';

export interface WorkSite {
  id: string;
  code: string;
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  timezone?: string;
  gpsAccuracyMaxM?: number;
  requirePhoto?: boolean;
  requireBiometric?: boolean;
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
  requirePhoto?: boolean;
  requireBiometric?: boolean;
}

export interface UpdateWorkSite {
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  timezone?: string;
  gpsAccuracyMaxM?: number;
  requirePhoto?: boolean;
  requireBiometric?: boolean;
}
