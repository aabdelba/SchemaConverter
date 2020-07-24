package com.bassboy.schemaevolver.test;

import com.bassboy.configuration.SchemaEvolverBeans;
import com.bassboy.controllers.UiController;
import com.bassboy.services.SchemaResourceManager;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
//@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SchemaEvolverBeans.class})
@TestPropertySource(locations = "file:./src/test/resources/test.properties")
//@WebMvcTest(controllers = { UiController.class })
@WebMvcTest(UiController.class)
@TestExecutionListeners(listeners={ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
public class FormUiController {

    @MockBean(name="schemaResourceManager")
    SchemaResourceManager resourceManager;

    @Autowired
    private MockMvc mvc;

//    @Value("${custom.schemaEvolver.ioDir}") private String ioDir;

    @Test
//    @WithMockUser//(username = "hulk", password = "smash", roles = "USER")
    @WithUserDetails("hulk")
    public void downloadDemo() throws Exception {
//        String ioDir = "SchemaEvolverIO";
//        File inputFile = new File(ioDir + "/output/json/file.json");
//        inputFile.createNewFile();
//
//        RequestBuilder request = MockMvcRequestBuilders.get("/form");
////        RequestBuilder request = MockMvcRequestBuilders.get("/download/json");
////        MvcResult result = mvc.perform(request).andReturn();
//        MvcResult result = mvc.perform(request)
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk())
//                .andReturn();




//        verify(resourceManager,times(1)).download(any(ZipOutputStream.class));
    }

    // TODO test download results

}
