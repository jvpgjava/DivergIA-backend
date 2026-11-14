package com.divergia.domain.model;

public record TriagemDaEntrada(
        MotivoEntradaInvalida motivo,
        CampoEntrada campo,
        String idiomaOriginal,
        String idiomaEditado,
        double confianca) {

    public TriagemDaEntrada {
        confianca = Double.isNaN(confianca) ? 0.0 : Math.max(0.0, Math.min(1.0, confianca));
        if (motivo != null && campo == null) {
            campo = CampoEntrada.AMBOS;
        }
    }

    public static TriagemDaEntrada valida() {
        return new TriagemDaEntrada(null, null, null, null, 1.0);
    }

    public boolean ehValida() {
        return motivo == null;
    }
}
