package com.soulmv.iam.enums;

/**
 * Perfis de acesso do sistema. Cada Role vira a authority "ROLE_<NOME>"
 * usada nas regras @PreAuthorize.
 */
public enum Role {

    /** TI / Infraestrutura. Administra o sistema, SEM acesso ao conteúdo clínico. */
    ADMIN,

    /** Médico / corpo clínico. Acesso completo de leitura/escrita ao prontuário. */
    MEDICO,

    /** Enfermagem. Triagem, sinais vitais, evolução de enfermagem, checagem de medicação. */
    ENFERMEIRO,

    /** Recepção. Cadastro demográfico, convênios, agendamento. SEM acesso clínico. */
    RECEPCAO,

    /** Gestão administrativa/financeira. Dashboards e faturamento (clínico restrito). */
    FATURAMENTO,

    /** Portal do paciente. Acesso de leitura apenas aos próprios dados. */
    PACIENTE
}
