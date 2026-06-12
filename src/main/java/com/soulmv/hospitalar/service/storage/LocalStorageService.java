package com.soulmv.hospitalar.service.storage;

import com.soulmv.hospitalar.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Armazenamento de arquivos no filesystem local (diretório configurável em
 * app.storage.local.base-path). Em produção pode ser substituído por S3/MinIO.
 */
@Service
public class LocalStorageService implements StorageService {

    private final Path baseDir;

    public LocalStorageService(@Value("${app.storage.local.base-path:./storage}") String basePath) {
        this.baseDir = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar o diretório de storage: " + baseDir, e);
        }
    }

    @Override
    public String armazenar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Arquivo vazio.");
        }
        String original = StringUtils.cleanPath(
                arquivo.getOriginalFilename() == null ? "arquivo" : arquivo.getOriginalFilename());
        String extensao = StringUtils.getFilenameExtension(original);
        String nomeArmazenado = UUID.randomUUID() + (extensao != null ? "." + extensao : "");

        Path destino = baseDir.resolve(nomeArmazenado).normalize();
        if (!destino.startsWith(baseDir)) {
            throw new BusinessException("Caminho de arquivo inválido.");
        }
        try {
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Falha ao gravar o arquivo: " + e.getMessage());
        }
        return nomeArmazenado;
    }

    @Override
    public Resource carregar(String nomeArmazenado) {
        Path arquivo = baseDir.resolve(nomeArmazenado).normalize();
        if (!arquivo.startsWith(baseDir)) {
            throw new BusinessException("Caminho de arquivo inválido.");
        }
        try {
            Resource resource = new UrlResource(arquivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("Arquivo não encontrado no storage.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BusinessException("Arquivo inválido: " + e.getMessage());
        }
    }
}
