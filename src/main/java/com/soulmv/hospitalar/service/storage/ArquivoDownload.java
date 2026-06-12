package com.soulmv.hospitalar.service.storage;

import org.springframework.core.io.Resource;

/**
 * Dados necessários para devolver um arquivo em download.
 */
public record ArquivoDownload(Resource recurso, String nomeOriginal, String contentType) {
}
