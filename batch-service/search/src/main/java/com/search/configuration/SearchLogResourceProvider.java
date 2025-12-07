package com.search.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLogResourceProvider {

    public Resource[] createResources(String directory, String pattern) {
        Path dirPath = Paths.get(directory);

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            log.warn("디렉토리가 존재하지 않습니다: {}", directory);
            return new Resource[0];
        }

        List<Resource> resources = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, pattern)) {
            List<Path> sortedPaths = new ArrayList<>();
            stream.forEach(sortedPaths::add);

            sortedPaths.sort(Comparator.comparing(p -> p.getFileName().toString()));

            for (Path filePath : sortedPaths) {
                if (Files.isRegularFile(filePath) && Files.isReadable(filePath)) {
                    resources.add(createResource(filePath.toString()));
                    log.info("로그 파일 추가: {}", filePath);
                }
            }
        } catch (IOException e) {
            log.error("디렉토리 읽기 실패: {}", directory, e);
            throw new IllegalArgumentException("디렉토리를 읽을 수 없습니다: " + directory, e);
        }

        log.info("총 {} 개의 로그 파일을 찾았습니다. 디렉토리: {}, 패턴: {}",
                resources.size(), directory, pattern);

        return resources.toArray(new Resource[0]);
    }

    /**
     * 주어진 경로에서 Resource를 생성
     * .gz 확장자인 경우 GZIP 압축 해제
     *
     * @param logPath 로그 파일 경로
     * @return Resource 객체
     */
    public Resource createResource(String logPath) {
        validateLogFile(logPath);

        if (logPath.endsWith(".gz")) {
            return createGzipResource(logPath);
        }
        return new FileSystemResource(logPath);
    }

    /**
     * 파일 존재 여부와 읽기 권한을 검증
     */
    private void validateLogFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("로그 파일을 찾을 수 없습니다: " + path);
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("로그 파일을 읽을 수 없습니다: " + path);
        }
        log.debug("로그 파일 검증 완료: {}", path);
    }

    /**
     * GZIP 압축 파일을 위한 Resource 생성
     */
    private Resource createGzipResource(String logPath) {
        try {
            File file = new File(logPath);
            String filename = file.getName();
            FileInputStream fileInputStream = new FileInputStream(file);
            return new InputStreamResource(fileInputStream) {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new GZIPInputStream(super.getInputStream());
                }

                @Override
                public String getDescription() {
                    return "GZIP resource [" + logPath + "]";
                }

                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("로그 파일을 찾을 수 없습니다: " + logPath, e);
        }
    }
}
