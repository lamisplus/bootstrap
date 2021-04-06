package org.lamisplus.modules.bootstrap.module;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class ModuleFileStorageService {
    private final Path rootLocation;

    public ModuleFileStorageService(ApplicationProperties properties) {
        this.rootLocation = Paths.get(properties.getModulePath());
    }

    public String store(String module, MultipartFile file) {
        module = module.toLowerCase();
        String filename = module + File.separator + StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.endsWith(File.separator)) {
            filename = filename.substring(0, filename.length() - 1) + ".jar";
        }
        if (!filename.endsWith(".jar")) {
            filename = filename + ".jar";
        }
        if (!Files.exists(rootLocation.resolve(module))) {
            try {
                Files.createDirectories(rootLocation.resolve(module));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + filename);
            }

            if (filename.contains("..")) {
                throw new RuntimeException("Cannot store file with relative path outside current directory " + filename);
            }

            try (InputStream inputStream = file.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, this.rootLocation.resolve(filename).toFile());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
        return filename;
    }

    public String store(String module, String name, InputStream inputStream) {
        module = module.toLowerCase();
        String filename = module + File.separator + StringUtils.cleanPath(name);
        if (filename.endsWith(File.separator)) {
            filename = filename.substring(0, filename.length() - 1) + ".jar";
        }
        if (!filename.endsWith(".jar")) {
            filename = filename + ".jar";
        }
        if (!Files.exists(rootLocation.resolve(module))) {
            try {
                Files.createDirectories(rootLocation.resolve(module));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (filename.contains("..")) {
                throw new RuntimeException("Cannot store file with relative path outside current directory " + filename);
            }
            FileUtils.copyInputStreamToFile(inputStream, this.rootLocation.resolve(filename).toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
        return filename;
    }

    public InputStream readFile(String file) throws FileNotFoundException {
        return new FileInputStream(rootLocation.resolve(file).toFile());
    }

    public URL getURL(String file) {
        try {
            return rootLocation.resolve(file).toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFile(String file) {
        try {
            FileSystemUtils.deleteRecursively(rootLocation.resolve(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        if (!Files.exists(rootLocation)) {
            try {
                Files.createDirectories(rootLocation);
            } catch (IOException e) {
                LOG.warn("Could not initialize storage: {}", e);
            }
        }
    }
}
