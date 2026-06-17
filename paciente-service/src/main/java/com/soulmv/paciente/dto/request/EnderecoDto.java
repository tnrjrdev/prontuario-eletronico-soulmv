package com.soulmv.paciente.dto.request;

/**
 * Endereço usado nos DTOs de entrada/saída.
 */
public record EnderecoDto(
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep
) {
}
