package com.condor.nexussoft.timeclock.tenancy.application;

import com.condor.nexussoft.timeclock.shared.domain.DomainException;
import com.condor.nexussoft.timeclock.tenancy.domain.CompanySettings;
import com.condor.nexussoft.timeclock.tenancy.domain.port.in.CompanySettingsUseCase;
import com.condor.nexussoft.timeclock.tenancy.domain.port.out.CompanySettingsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanySettingsServiceTest {

    @Mock CompanySettingsRepositoryPort repository;
    @InjectMocks CompanySettingsService service;

    final UUID tenantId = UUID.randomUUID();

    private CompanySettingsUseCase.UpdateCommand command(Integer accuracy, String action) {
        return new CompanySettingsUseCase.UpdateCommand(accuracy, true, false, true, action);
    }

    /** Sin fila de configuración el tenant opera con los defaults del esquema, no con un 404. */
    @Test
    void tenantSinConfiguracion_devuelveLosValoresPorDefecto() {
        when(repository.find(tenantId)).thenReturn(Optional.empty());

        CompanySettings settings = service.get(tenantId);

        assertThat(settings.requirePhoto()).isFalse();
        assertThat(settings.requireBiometric()).isFalse();
        assertThat(settings.deviceBindingEnabled()).isTrue();
        assertThat(settings.deviceBindingAction()).isEqualTo(CompanySettings.DeviceBindingAction.REJECT);
    }

    @Test
    void actualizar_persisteLaPoliticaDeFoto() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CompanySettings saved = service.update(tenantId, command(50, "FLAG"));

        assertThat(saved.requirePhoto()).isTrue();
        assertThat(saved.deviceBindingAction()).isEqualTo(CompanySettings.DeviceBindingAction.FLAG);
    }

    @Test
    void precisionGpsFueraDeRango_esRechazada() {
        assertThatThrownBy(() -> service.update(tenantId, command(4000, "REJECT")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("precisión GPS");
    }

    @Test
    void accionDeDispositivoDesconocida_esRechazada() {
        assertThatThrownBy(() -> service.update(tenantId, command(50, "BORRAR")))
                .isInstanceOf(DomainException.class);
    }
}
