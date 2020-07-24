package com.bassboy.schemaevolver.test;

import com.bassboy.configuration.SchemaEvolverBeans;
import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidSchemaEntryException;
import com.bassboy.schemaevolver.JsonSchemaEvolver;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.services.SchemaResourceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(SpringRunner.class)//needed for autowire and dependency injection
@ContextConfiguration(classes = {SchemaEvolverBeans.class})
@TestPropertySource(locations = "file:./src/test/resources/test.properties")
public class ioFilesObjectInteraction extends TestUtils {

    @Mock private JsonSchemaEvolver schemaEvolver;
    @Mock private FormModel formModel;
    @InjectMocks @Spy private SchemaResourceManager resourceManager;
    @Value("${custom.schemaEvolver.ioDir}") private String ioDir;

    @Test
    public void runConversionOnce() throws SchemaEvolverException, InvalidSchemaEntryException, IOException {
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                    File inputFile = new File(ioDir + "/input/record/file.json");
                    inputFile.createNewFile();
                    return null;
                }
            }
        ).when(resourceManager).writeMultifileToInputDirectories(any(FormModel.class));

        runConversion(1);
    }

    @Test
    public void runConversionThrice() throws SchemaEvolverException, InvalidSchemaEntryException, IOException {
        Mockito.doAnswer(
                new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                        File inputFile1 = new File(ioDir + "/input/record/file1.json");
                        File inputFile2 = new File(ioDir + "/input/record/file2.json");
                        File inputFile3 = new File(ioDir + "/input/record/file3.json");
                        inputFile1.createNewFile();
                        inputFile2.createNewFile();
                        inputFile3.createNewFile();
                        return null;
                    }
                }
        ).when(resourceManager).writeMultifileToInputDirectories(any(FormModel.class));

        runConversion(3);
    }

    private void runConversion(int numberOfTimesMethodShouldRun) throws IOException, SchemaEvolverException, InvalidSchemaEntryException {
        Mockito.doNothing().when(resourceManager).writeTextboxToInputDirectories(formModel);

        when(schemaEvolver.getIoDir()).thenReturn(ioDir);
        when(formModel.getOldSchemaFile()).thenReturn(createMockMultipartFileForTest("oldSchema",""));
        when(formModel.getNewSchemaFile()).thenReturn(createMockMultipartFileForTest("newSchema",""));
        when(formModel.getRenamedFile()).thenReturn(createMockMultipartFileForTest("renamedFields",""));

        resourceManager.runConversion(formModel);

        verify(resourceManager,times(1)).init();
        verify(schemaEvolver,times(numberOfTimesMethodShouldRun))
                .convertDataAndPlaceInOutputDir(anyString(),anyString(),anyString(),anyString(),anyString());

    }

}
