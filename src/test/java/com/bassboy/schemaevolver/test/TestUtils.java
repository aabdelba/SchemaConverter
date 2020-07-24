package com.bassboy.schemaevolver.test;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class TestUtils {
    public MultipartFile createMockMultipartFileForTest(String fileName, String fileContent) throws IOException {
        byte[] content = fileContent.getBytes();
        return new MockMultipartFile(fileName,fileName,"text/plain",content);
    }
}
