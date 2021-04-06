package org.lamisplus.modules.bootstrap.module;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.lamisplus.modules.bootstrap.yml.ModuleConfig;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ModuleUtils {

    public static void copyPathFromJar(final URL jarPath, final String path, final Path target) throws Exception {
        Map<String, String> env = new HashMap<>();
        String absPath = jarPath.toString();
        URI uri = URI.create("jar:" + absPath);
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path pathInZipfile = zipfs.getPath(path);
            Files.walkFileTree(pathInZipfile, new SimpleFileVisitor<Path>() {

                private Path currentTarget;

                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    currentTarget = target.resolve(pathInZipfile.relativize(dir)
                            .toString());
                    if (!Files.exists(currentTarget)) {
                        Files.createDirectories(currentTarget);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(pathInZipfile.relativize(file)
                            .toString()), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    static void addClassPathUrl(URL url, ClassLoader classLoader) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ModuleConfig loadModuleConfig(InputStream zip, String name) throws Exception {
        try (ZipInputStream zin = new ZipInputStream(zip)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().endsWith(".jar")) {
                    return loadModuleConfig(zin, name);
                }
                if (entry.getName().equals(name)) {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(zin));
                    Yaml yaml = new Yaml(new Constructor(ModuleConfig.class));
                    return yaml.load(in);
                }
            }
        }
        throw new RuntimeException("No module information in uploaded file");
    }

    @SneakyThrows
    public static void extract(String resourceFolder, String destinationFolder) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourceFolder + "/**");
        URI inJarUri = new DefaultResourceLoader().getResource("classpath:" + resourceFolder).getURI();
        for (Resource resource : resources) {
            String relativePath = resource.getURI().getRawSchemeSpecificPart().replace(inJarUri.getRawSchemeSpecificPart(), "");
            if (relativePath.isEmpty()) {
                continue;
            }
            File dirFile = new File(destinationFolder + relativePath);
            if (!resource.isReadable()) {
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }
            } else {
                if (!dirFile.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        IOUtils.copy(is, new FileOutputStream(dirFile));
                    }
                }
            }
        }
    }
}
