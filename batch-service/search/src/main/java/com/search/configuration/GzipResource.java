package com.search.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.springframework.core.io.FileSystemResource;


public class GzipResource extends FileSystemResource {

    public GzipResource(String path) {
        super(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new GZIPInputStream(super.getInputStream());
    }

    @Override
    public String getDescription() {
        return "GZIP resource [" + getPath() + "]";
    }
}
