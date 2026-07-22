export type ProjectStatus = 'ACTIVE' | 'INACTIVE' | 'CLOSED';

export const PROJECT_STATUSES: ProjectStatus[] = ['ACTIVE', 'INACTIVE', 'CLOSED'];

export interface Project {
  id: string;
  code: string;
  name: string;
  status: ProjectStatus;
  startsOn?: string;
  endsOn?: string;
}

export interface CreateProject {
  code: string;
  name: string;
  startsOn?: string;
  endsOn?: string;
}

export interface UpdateProject {
  name: string;
  status: ProjectStatus;
  startsOn?: string;
  endsOn?: string;
}
