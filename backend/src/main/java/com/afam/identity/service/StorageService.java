package com.afam.identity.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    /**
     * Memorizza un file all'interno della cartella specifica dell'utente.
     * @param file Il file da salvare
     * @param userUuid L'UUID dell'utente che funge da sotto-cartella
     * @return Il percorso relativo salvato
     */
    String store(MultipartFile file, String userUuid);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
