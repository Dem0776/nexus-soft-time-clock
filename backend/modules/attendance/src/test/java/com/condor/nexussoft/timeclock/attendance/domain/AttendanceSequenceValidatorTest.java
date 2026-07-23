package com.condor.nexussoft.timeclock.attendance.domain;

import com.condor.nexussoft.timeclock.attendance.domain.AttendanceSequenceValidator.LastEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.condor.nexussoft.timeclock.attendance.domain.AttendanceEventType.*;
import static org.assertj.core.api.Assertions.assertThat;

class AttendanceSequenceValidatorTest {

    private final UUID site = UUID.randomUUID();
    private final UUID otherSite = UUID.randomUUID();

    private Optional<RejectionReason> validate(AttendanceEventType prev, AttendanceEventType requested, UUID reqSite) {
        Optional<LastEvent> last = prev == null ? Optional.empty() : Optional.of(new LastEvent(prev, site));
        return AttendanceSequenceValidator.validate(last, requested, reqSite);
    }

    @Test
    void entrada_sinJornadaPrevia_esValida() {
        assertThat(validate(null, ENTRADA, site)).isEmpty();
    }

    @Test
    void entrada_trasSalida_esValida() {
        assertThat(validate(SALIDA, ENTRADA, site)).isEmpty();
    }

    @Test
    void entrada_conJornadaAbierta_esRechazada() {
        assertThat(validate(ENTRADA, ENTRADA, site)).contains(RejectionReason.INVALID_SEQUENCE);
    }

    @Test
    void salida_sinEntradaAbierta_esRechazada() {
        assertThat(validate(null, SALIDA, site)).contains(RejectionReason.INVALID_SEQUENCE);
        assertThat(validate(SALIDA, SALIDA, site)).contains(RejectionReason.INVALID_SEQUENCE);
    }

    @Test
    void salida_conEntradaAbierta_mismoCentro_esValida() {
        assertThat(validate(ENTRADA, SALIDA, site)).isEmpty();
    }

    @Test
    void salida_enOtroCentro_esRechazada() {
        assertThat(validate(ENTRADA, SALIDA, otherSite)).contains(RejectionReason.INVALID_SEQUENCE);
    }

    @Test
    void inicioDescanso_trabajando_esValido_peroDobleDescanso_esRechazado() {
        assertThat(validate(ENTRADA, INICIO_DESCANSO, site)).isEmpty();
        assertThat(validate(INICIO_DESCANSO, INICIO_DESCANSO, site)).contains(RejectionReason.INVALID_SEQUENCE);
    }

    @Test
    void finDescanso_soloTrasInicioDescanso() {
        assertThat(validate(INICIO_DESCANSO, FIN_DESCANSO, site)).isEmpty();
        assertThat(validate(ENTRADA, FIN_DESCANSO, site)).contains(RejectionReason.INVALID_SEQUENCE);
    }

    @Test
    void salida_trasFinDescanso_esValida() {
        assertThat(validate(FIN_DESCANSO, SALIDA, site)).isEmpty();
    }

    @Test
    void cambioSitio_requiereJornadaAbierta() {
        assertThat(validate(ENTRADA, CAMBIO_SITIO, site)).isEmpty();
        assertThat(validate(INICIO_DESCANSO, CAMBIO_SITIO, site)).contains(RejectionReason.INVALID_SEQUENCE);
    }
}
