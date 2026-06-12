package com.soulmv.hospitalar.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Abstração de armazenamento de arquivos (laudos, anexos).
 */
public interface StorageService {

    /** Grava o arquivo e retorna o nome único com que foi armazenado. */
    String armazenar(MultipartFile arquivo);

    /** Carrega o arquivo previamente armazenado como Resource para download. */
    Resource carregar(String nomeArmazenado);
}
