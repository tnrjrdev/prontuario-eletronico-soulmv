package com.soulmv.hospitalar.dto.request;

import com.soulmv.hospitalar.enums.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Dados para cadastro/edição de paciente.
 */
public record PacienteRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos (somente números)")
        String cpf,

        String cartaoSus,

        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        Sexo sexo,

        String telefone,

        @Email(message = "E-mail inválido")
        String email,

        @Valid
        EnderecoDto endereco,

        Long convenioId,

        String numeroCarteirinha
) {
}
