package com.condor.nexussoft.timeclock.attendance.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.condor.nexussoft.timeclock.attendance.domain.AttendanceEventType.*;
import static org.assertj.core.api.Assertions.assertThat;

class EventTypeCatalogTest {

    @Test
    void entradaYSalida_sonNucleo_siempreHabilitadas() {
        assertThat(EventTypeCatalog.isConfigurable(ENTRADA)).isFalse();
        assertThat(EventTypeCatalog.isConfigurable(SALIDA)).isFalse();
        // Aun con un override deshabilitándolas, siguen habilitadas.
        Map<AttendanceEventType, EventTypeSetting> stored = Map.of(
                ENTRADA, new EventTypeSetting(ENTRADA, false, "x"));
        assertThat(EventTypeCatalog.isEnabled(ENTRADA, stored)).isTrue();
    }

    @Test
    void intermedios_habilitadosPorDefecto_sinConfig() {
        assertThat(EventTypeCatalog.isConfigurable(INICIO_DESCANSO)).isTrue();
        assertThat(EventTypeCatalog.isEnabled(INICIO_DESCANSO, Map.of())).isTrue();
    }

    @Test
    void intermedioDeshabilitado_porOverride() {
        Map<AttendanceEventType, EventTypeSetting> stored = Map.of(
                CAMBIO_SITIO, new EventTypeSetting(CAMBIO_SITIO, false, "Cambio"));
        assertThat(EventTypeCatalog.isEnabled(CAMBIO_SITIO, stored)).isFalse();
    }

    @Test
    void merge_devuelveLos5_conEtiquetasPorDefecto_yOverrides() {
        Map<AttendanceEventType, EventTypeSetting> stored = Map.of(
                FIN_DESCANSO, new EventTypeSetting(FIN_DESCANSO, false, "Regreso"));
        List<EventTypeSetting> all = EventTypeCatalog.merge(stored);

        assertThat(all).hasSize(5);
        EventTypeSetting fin = all.stream().filter(s -> s.eventType() == FIN_DESCANSO).findFirst().orElseThrow();
        assertThat(fin.enabled()).isFalse();
        assertThat(fin.label()).isEqualTo("Regreso");   // override respetado
        EventTypeSetting inicio = all.stream().filter(s -> s.eventType() == INICIO_DESCANSO).findFirst().orElseThrow();
        assertThat(inicio.enabled()).isTrue();
        assertThat(inicio.label()).isEqualTo("Inicio de descanso");  // etiqueta por defecto
    }
}
