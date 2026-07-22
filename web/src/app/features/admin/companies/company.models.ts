export type CompanyStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export interface Company {
  id: string;
  code: string;
  name: string;
  legalName?: string;
  emailDomain?: string;
  timezone: string;
  locale: string;
  status: CompanyStatus;
}

export interface CreateCompany {
  code: string;
  name: string;
  legalName?: string;
  emailDomain?: string;
  timezone?: string;
  locale?: string;
}

export interface UpdateCompany {
  name: string;
  legalName?: string;
  emailDomain?: string;
  timezone?: string;
  locale?: string;
}
