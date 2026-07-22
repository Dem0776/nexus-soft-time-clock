# ADR-003 — Hora del servidor como autoridad temporal

**Estado:** Aceptado · **Fecha:** 2026-07-21

## Contexto
La hora del dispositivo es manipulable (fraude) y difiere entre equipos. La asistencia requiere un timestamp confiable (RF-17, RN-11).

## Decisión
El **timestamp oficial** de todo registro es la **hora del servidor** al procesarlo (UTC en almacenamiento; zona del centro/tenant para cálculo y presentación, RNF-19). La hora del dispositivo se guarda solo como **metadato** para diagnóstico/antifraude (detectar desfase sospechoso). Para registros **offline**, el servidor fija la hora al **sincronizar** y conserva la hora local declarada como referencia; discrepancias grandes generan bandera/incidencia.

## Consecuencias
- ➕ Integridad temporal a prueba de manipulación del dispositivo.
- ➕ Consistencia entre dispositivos y husos.
- ➖ Registros offline no reflejan la hora exacta del evento físico → se documenta la política y se marca el desfase; se conserva la hora declarada para auditoría.
- ➖ Requiere NTP fiable en servidores.
