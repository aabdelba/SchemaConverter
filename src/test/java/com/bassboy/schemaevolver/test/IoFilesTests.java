package com.bassboy.schemaevolver.test;

import com.bassboy.common.RwUtils;
import com.bassboy.configuration.SchemaEvolverBeans;
import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidSchemaEntryException;
import com.bassboy.schemaevolver.JsonSchemaEvolver;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.services.SchemaEvolverFactory;
import com.bassboy.services.SchemaResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)//needed for autowire and dependency injection
@ContextConfiguration(classes = {SchemaEvolverBeans.class})
//@SpringBootTest(classes = LaunchSchemaEvolverServiceMain.class)
//@TestPropertySource(locations = "application.properties")
@TestPropertySource(locations = "file:./src/test/resources/test.properties")
//@TestPropertySource(properties = {
//        "custom.schemaEvolver.ioDir=SchemaEvolverIO",
//})
public class IoFilesTests extends TestUtils{

    @Autowired
    @Qualifier("schemaResourceManager")
    private SchemaResourceManager resourceManager;

//    @Mock SchemaEvolverFactory schemaEvolverFactory;
//    @InjectMocks private SchemaResourceManager resourceManager;
    @Value("${custom.schemaEvolver.ioDir}") private String ioDir;
    private File recordDir;
    private File schemaDir;
    private File avroDir;
    private File jsonDir;

    @Before
    public void setDirectoriesForTest() throws IOException {
        recordDir = new File(ioDir+"/input/record");
        schemaDir = new File(ioDir+"/input/schema");
        avroDir = new File(ioDir+"/output/avro");
        jsonDir = new File(ioDir+"/output/json");
    }

    @Test
    public void initMethodClearsDirectories() throws IOException {
        File recordFile1 = new File(recordDir.getAbsolutePath()+"/testFile1.txt");
        File recordFile2 = new File(recordDir.getAbsolutePath()+"/testFile2.txt");
        File schemaFile1 = new File(schemaDir.getAbsolutePath()+"/testFile1.txt");
        File schemaFile2 = new File(schemaDir.getAbsolutePath()+"/testFile2.txt");
        File jsonFile1 = new File(jsonDir.getAbsolutePath()+"/testFile1.txt");
        File jsonFile2 = new File(jsonDir.getAbsolutePath()+"/testFile2.txt");
        File avroFile1 = new File(avroDir.getAbsolutePath()+"/testFile1.txt");
        File avroFile2 = new File(avroDir.getAbsolutePath()+"/testFile2.txt");

        createTestFile(recordFile1);
        createTestFile(recordFile2);
        createTestFile(schemaFile1);
        createTestFile(schemaFile2);
        createTestFile(jsonFile1);
        createTestFile(jsonFile2);
        createTestFile(avroFile1);
        createTestFile(avroFile2);
        File dummyDir = new File(recordDir.getAbsolutePath()+"/testDir.txt");
        dummyDir.mkdir();
        assert dummyDir.exists()
                : "Failed to create directory: " + dummyDir.getAbsolutePath();

        resourceManager.init();

        assert recordDir.isDirectory()
                : recordDir.getAbsolutePath() + " is not a directory";
        assert recordDir.list().length == 0
                : recordDir.getName() + " is not empty";

        assert schemaDir.isDirectory()
                : schemaDir.getAbsolutePath() + " is not a directory";
        assert schemaDir.list().length == 0
                : schemaDir.getName() + " is not empty";

        assert avroDir.isDirectory()
                : avroDir.getAbsolutePath() + " is not a directory";
        assert avroDir.list().length == 0
                : avroDir.getName() + " is not empty";

        assert jsonDir.isDirectory()
                : jsonDir.getAbsolutePath() + " is not a directory";
        assert jsonDir.list().length == 0
                : jsonDir.getName() + " is not empty";
    }

    // a file is created for each textbox entry
    @Test
    public void textboxEntriesAreParsedIntoFilesInSchemaDir() throws IOException {

        File expectedFile;
        String fileTextContent = "test supplimentary files";
        resourceManager.init();

        FormModel model = new FormModel();
        model.setOldSchemaText(fileTextContent);
        expectedFile = new File(schemaDir + "/oldSchema.avsc");
        resourceManager.writeTextboxToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);

        model = new FormModel();
        model.setNewSchemaText(fileTextContent);
        expectedFile = new File(schemaDir + "/newSchema.avsc");
        resourceManager.writeTextboxToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);

        model = new FormModel();
        model.setRenamedText(fileTextContent);
        expectedFile = new File(schemaDir + "/renamedFields.txt");
        resourceManager.writeTextboxToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);

    }

    @Test
    public void oneTextboxRecordIsParsedIntoFilesInRecordDir() throws IOException {

        File expectedFile;
        FormModel model = new FormModel();

        resourceManager.init();
        String fileTextContent = "record dummy";
        model.setOldJsonText(fileTextContent);
        expectedFile = new File(recordDir + "/textboxRecord1.json");
        resourceManager.writeTextboxToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);
    }

    @Test
    public void multipleTextboxRecordsAreParsedIntoFilesInRecordDir() throws IOException {

        File expectedFile;
        FormModel model = new FormModel();

        resourceManager.init();
        String fileTextContent = "record1 ;;; record2 ;;; record3 ;; record4";
        model.setOldJsonText(fileTextContent);
        resourceManager.writeTextboxToInputDirectories(model);

        expectedFile = new File(recordDir + "/textboxRecord1.json");
        schemaFileAssertions(expectedFile,"record1");
        expectedFile = new File(recordDir + "/textboxRecord2.json");
        schemaFileAssertions(expectedFile,"record2");
        expectedFile = new File(recordDir + "/textboxRecord3.json");
        schemaFileAssertions(expectedFile,"record3 ;; record4");
    }

    @Test
    public void multipartFileEntriesAreParsedIntoSchemaDir() throws IOException {

        String fileTextContent = "test supplimentary files";
        resourceManager.init();

        MultipartFile actualMultipartFile = createMockMultipartFileForTest("oldSchemaTest.txt",fileTextContent);
        File expectedFile = new File(schemaDir + "/" + actualMultipartFile.getOriginalFilename());

        FormModel model = new FormModel();
        model.setOldSchemaFile(actualMultipartFile);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);

        actualMultipartFile = createMockMultipartFileForTest("newSchemaTest.txt",fileTextContent);
        expectedFile = new File(schemaDir + "/" + actualMultipartFile.getOriginalFilename());
        model = new FormModel();
        model.setNewSchemaFile(actualMultipartFile);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);

        actualMultipartFile = createMockMultipartFileForTest("renamedTest.txt",fileTextContent);
        expectedFile = new File(schemaDir + "/" + actualMultipartFile.getOriginalFilename());
        model = new FormModel();
        model.setRenamedFile(actualMultipartFile);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);
    }

    @Test
    public void oneMultipartFileRecordIsParsedIntoFileInRecordDir() throws IOException {

        String fileTextContent = "record file";
        MultipartFile actualMultipartFile = createMockMultipartFileForTest("test.txt",fileTextContent);
        File expectedFile = new File(recordDir + "/" + actualMultipartFile.getOriginalFilename());
        MultipartFile[] actualMultipartFiles = {actualMultipartFile};

        resourceManager.init();
        FormModel model = new FormModel();
        model.setOldJsonFiles(actualMultipartFiles);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);
    }

    @Test
    public void multipleMultipartFileRecordsAreParsedIntoFilesInRecordDir() throws IOException {

        File expectedFile;
        String fileTextContent = "record file";
        MultipartFile actualMultipartFile1 = createMockMultipartFileForTest("test1.txt",fileTextContent+" 1");
        MultipartFile actualMultipartFile2 = createMockMultipartFileForTest("test2.txt",fileTextContent+" 2");
        MultipartFile actualMultipartFile3 = createMockMultipartFileForTest("test3.txt",fileTextContent+" 3");
        MultipartFile[] actualMultipartFiles = {actualMultipartFile1,actualMultipartFile2,actualMultipartFile3};

        resourceManager.init();
        FormModel model = new FormModel();
        model.setOldJsonFiles(actualMultipartFiles);
        resourceManager.writeMultifileToInputDirectories(model);

        int i = 1;
        for (MultipartFile actualMultipartFile : actualMultipartFiles) {
            expectedFile = new File(recordDir + "/" + actualMultipartFile.getOriginalFilename());
            schemaFileAssertions(expectedFile,fileTextContent+" "+i);
            i++;
        }
    }

    @Test
    public void multipartFilesWithTheSameName() throws IOException {

        String fileTextContent = "test files";
        String fileNameWithoutExtension = "textboxRecord1";
        String fileExtension = ".json";
        resourceManager.init();

        MultipartFile actualMultipartFile = createMockMultipartFileForTest(fileNameWithoutExtension+fileExtension,fileTextContent);

        FormModel model = new FormModel();
        model.setOldSchemaFile(actualMultipartFile);
        model.setNewSchemaFile(actualMultipartFile);
        model.setRenamedFile(actualMultipartFile);

        MultipartFile[] actualMultipartFiles = {actualMultipartFile,actualMultipartFile};
        model.setOldJsonFiles(actualMultipartFiles);

        model.setOldJsonText("json1;;;json2");
        resourceManager.writeTextboxToInputDirectories(model);
        resourceManager.writeMultifileToInputDirectories(model);

        File oldSchemaFile = new File(schemaDir+"/"+fileNameWithoutExtension+fileExtension);
        assert oldSchemaFile.exists() : "File with duplicate name was not created " + oldSchemaFile;
        File newSchemaFile = new File(schemaDir+"/"+fileNameWithoutExtension+"_renamed"+fileExtension);
        assert newSchemaFile.exists() : "File with duplicate name was not created " + newSchemaFile;
        File renamedFile = new File(schemaDir+"/"+fileNameWithoutExtension+"_renamed_renamed"+fileExtension);
        assert renamedFile.exists() : "File with duplicate name was not created " + renamedFile;

        File oldJsonFile1 = new File(recordDir+"/"+fileNameWithoutExtension+fileExtension);
        assert oldJsonFile1.exists() : "File with duplicate name was not created " + oldJsonFile1;
        File oldJsonFile2 = new File(recordDir+"/"+fileNameWithoutExtension+"_renamed"+fileExtension);
        assert oldJsonFile2.exists() : "File with duplicate name was not created " + oldJsonFile2;
        File oldJsonFile3 = new File(recordDir+"/"+fileNameWithoutExtension+"_renamed_renamed"+fileExtension);
        assert oldJsonFile3.exists() : "File with duplicate name was not created " + oldJsonFile3;
        File oldJsonFile4 = new File(recordDir+"/textboxRecord2"+fileExtension);
        assert oldJsonFile3.exists() : "File with duplicate name was not created " + oldJsonFile4;

    }

    // multifile takes precedence over textbox for schema and renamed filess
    @Test
    public void multipartFileAndTextboxEntriesBothExistForSchemaDirectory() throws IOException {

        String fileTextContent = "multipart file";
        String textboxContent = "textbox file";
        resourceManager.init();

        MultipartFile actualMultipartFile = createMockMultipartFileForTest("oldSchemaTest.txt",fileTextContent);
        File expectedFile = new File(schemaDir + "/" + actualMultipartFile.getOriginalFilename());
        FormModel model = new FormModel();
        model.setOldSchemaFile(actualMultipartFile);
        model.setOldSchemaText(textboxContent);
        resourceManager.writeTextboxToInputDirectories(model);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);
        expectedFile = new File(schemaDir + "/oldSchema.avsc");
        assert !expectedFile.exists()
                : "Textbox file was created " + expectedFile.getAbsolutePath();

        actualMultipartFile = createMockMultipartFileForTest("newSchemaTest.txt",fileTextContent);
        expectedFile = new File(schemaDir + "/" + actualMultipartFile.getOriginalFilename());
        model = new FormModel();
        model.setNewSchemaFile(actualMultipartFile);
        model.setNewSchemaText(textboxContent);
        resourceManager.writeTextboxToInputDirectories(model);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);
        expectedFile = new File(schemaDir + "/newSchema.avsc");
        assert !expectedFile.exists()
                : "Textbox file was created " + expectedFile.getAbsolutePath();

        actualMultipartFile = createMockMultipartFileForTest("renamedTest.txt",fileTextContent);
        expectedFile = new File(schemaDir + "/" + actualMultipartFile.getOriginalFilename());
        model = new FormModel();
        model.setRenamedFile(actualMultipartFile);
        model.setRenamedText(textboxContent);
        resourceManager.writeTextboxToInputDirectories(model);
        resourceManager.writeMultifileToInputDirectories(model);
        schemaFileAssertions(expectedFile,fileTextContent);
        expectedFile = new File(schemaDir + "/renamedFields.txt");
        assert !expectedFile.exists()
                : "Textbox file was created " + expectedFile.getAbsolutePath();
    }

    private void createTestFile(File file) throws IOException {
        file.createNewFile();
        assert file.exists()
                : "Failed to create: " + file.getAbsolutePath();
    }

    private void schemaFileAssertions(File testFile, String fileTextContent) throws IOException {
        assert testFile.exists()
                : "Failed to create file: " + testFile.getAbsolutePath();
        String actualFileContent = RwUtils.convertFileContentToString(testFile);
        assert actualFileContent.trim().equals(fileTextContent.trim())
                : "Mismatch of text inside created file " + testFile.getAbsolutePath()
                + "\n  Expected: "+fileTextContent
                + "\n  Got: "+actualFileContent;
    }

}
