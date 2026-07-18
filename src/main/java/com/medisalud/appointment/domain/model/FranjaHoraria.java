package com.medisalud.appointment.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public class FranjaHoraria {
    private final UUID medicoId;
    private final OffsetDateTime inicio;
    private final OffsetDateTime fin;
    private final boolean disponible;

    public FranjaHoraria(UUID medicoId, OffsetDateTime inicio, OffsetDateTime fin, boolean disponible) {
        this.medicoId = medicoId;
        this.inicio = inicio;
        this.fin = fin;
        this.disponible = disponible;
    }

    public UUID getMedicoId() { return medicoId; }
    public OffsetDateTime getInicio() { return inicio; }
    public OffsetDateTime getFin() { return fin; }
    public boolean isDisponible() { return disponible; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FranjaHoraria that)) return false;
        return Objects.equals(medicoId, that.medicoId) && Objects.equals(inicio, that.inicio);
    }

    @Override
    public int hashCode() { return Objects.hash(medicoId, inicio); }
}
