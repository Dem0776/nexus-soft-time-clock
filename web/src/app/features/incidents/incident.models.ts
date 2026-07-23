export type IncidentResolution = 'APPROVED' | 'REJECTED' | 'RESOLVED';

export const INCIDENT_RESOLUTIONS: IncidentResolution[] = ['APPROVED', 'REJECTED', 'RESOLVED'];

export interface Incident {
  id: string;
  userId: string;
  userName?: string;
  type: string;
  status: string;
  priority: string;
  incidentDate: string;
  createdAt: string;
  relatedAttendanceId?: string;
  description?: string;
  resolutionNote?: string;
  resolvedBy?: string;
  resolvedAt?: string;
}

export interface ResolveIncident {
  status: IncidentResolution;
  note?: string;
}
