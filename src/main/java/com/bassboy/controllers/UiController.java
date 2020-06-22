package com.bassboy.controllers;

import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidSchemaEntryException;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.services.SchemaEvolverUserDetailsService;
import com.bassboy.services.SchemaResourceManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

@Controller
public class UiController {

    @Autowired
    private OAuth2AccessTokenResponseClient accessTokenResponseClient;

    @Autowired
    @Qualifier("schemaResourceManager")
    private SchemaResourceManager resourceManager;

    @Autowired
    SchemaEvolverUserDetailsService schemaEvolverUserDetailsService;

    @Autowired
    ObjectMapper mapper;

    private String displayName;

    @RequestMapping("/")
    public String welcome() {
        return "index";
    }

    @RequestMapping(value="form", method={RequestMethod.GET,RequestMethod.POST})
    public String schemaEvolverForm(HttpServletRequest request, HttpServletResponse response, Model model, Principal principal) throws IOException, ServletException {

        JsonNode root =  mapper.readTree(mapper.writeValueAsString(principal));
        DocumentContext documentContext = JsonPath.parse(root.toString());

        if(root.has("authorizedClientRegistrationId")) {
            HttpSession session = request.getSession(false);
            RequestDispatcher dispatcher = session.getServletContext()
                                        .getRequestDispatcher("/social/user");
            dispatcher.forward(request, response);
        }
        displayName = getNameFromPrincipalJson(root,documentContext);
        model.addAttribute("username", displayName);
        model.addAttribute("formModel", new FormModel());
        return "form";
    }

    @RequestMapping(value="/social/user", method = {RequestMethod.GET,RequestMethod.POST})
    public String socialSignin(Model model, Principal principal) throws IOException {
        JsonNode root =  mapper.readTree(mapper.writeValueAsString(principal));
        DocumentContext documentContext = JsonPath.parse(root.toString());

        String socialId = getSocialIdFromPrincipalJson(root,documentContext);
        displayName = getNameFromPrincipalJson(root,documentContext);
        schemaEvolverUserDetailsService.createSocialUserIfNotFound(socialId, displayName);
        model.addAttribute("username", displayName);
        model.addAttribute("formModel", new FormModel());
        return "form";
    }

    @RequestMapping(value="conversion",method = {RequestMethod.POST})
    public String schemaConversionLoading(@ModelAttribute("formModel") FormModel formModel, ModelMap model, Principal principal) throws IOException, SchemaEvolverException, InvalidSchemaEntryException {

        // Added wait time for UX
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        resourceManager.setFormModel(formModel);
        resourceManager.init();
        resourceManager.runConversion();
        model.addAttribute("resourceManager", resourceManager);
        model.addAttribute("username", displayName);

        return "complete";
    }

    @RequestMapping(path = "/download/{downloadFormat}", method = RequestMethod.GET)
    public void download(@PathVariable String downloadFormat, HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String s1;
        if(downloadFormat.equals("demo"))
            s1 = "attachment; filename=DemoFiles.zip";
        else
            s1 = "attachment; filename=SchemaEvolverResults_" + sdf.format(timestamp) + ".zip";
        response.setHeader("Content-Disposition", s1);
        try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
            FormModel formModel = resourceManager.getFormModel();
            if(formModel==null)
                formModel = new FormModel();
            formModel.setDownloadFormat(downloadFormat);
            resourceManager.download(zippedOut);
        }//if try and fail, zippedOut is closed
    }

    private String getSocialIdFromPrincipalJson(JsonNode root, DocumentContext documentContext) {
        if(root.has("authorizedClientRegistrationId"))
            return documentContext.read("$.authorizedClientRegistrationId")
                            + ":" + documentContext.read("$.name");
        else
            return "";
    }

    private String getNameFromPrincipalJson(JsonNode root, DocumentContext documentContext) {
        if(root.has("authorizedClientRegistrationId")) {
            if(documentContext.read("$.authorizedClientRegistrationId").toString().toLowerCase()
                    .equals("linkedin"))
                return documentContext.read("$.authorities[0].attributes.localizedFirstName")
                        + " " + documentContext.read("$.authorities[0].attributes.localizedLastName");
            else
                return documentContext.read("$.authorities[0].attributes.name");
        }
        else
            return documentContext.read("$.name");
    }

}
