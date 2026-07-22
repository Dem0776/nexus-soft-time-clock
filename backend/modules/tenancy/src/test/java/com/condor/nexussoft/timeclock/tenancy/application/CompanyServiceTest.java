package com.condor.nexussoft.timeclock.tenancy.application;

import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.tenancy.domain.Company;
import com.condor.nexussoft.timeclock.tenancy.domain.port.in.CompanyCommands;
import com.condor.nexussoft.timeclock.tenancy.domain.port.out.CompanyRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock CompanyRepositoryPort companies;

    @Test
    void create_conCodigoNuevo_persisteEmpresaActiva() {
        CompanyService service = new CompanyService(companies);
        when(companies.existsByCode("DEMO")).thenReturn(false);
        when(companies.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(new CompanyCommands.CreateCompanyCommand(
                "DEMO", "Empresa Demo", null, "demo.com", null, null));

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companies).save(captor.capture());
        assertThat(captor.getValue().code()).isEqualTo("DEMO");
        assertThat(captor.getValue().timezone()).isEqualTo("UTC");   // default aplicado
    }

    @Test
    void create_conCodigoDuplicado_lanzaDomainException() {
        CompanyService service = new CompanyService(companies);
        when(companies.existsByCode("DEMO")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CompanyCommands.CreateCompanyCommand(
                "DEMO", "Otra", null, null, null, null)))
                .isInstanceOf(DomainException.class);

        verify(companies, never()).save(any());
    }
}
