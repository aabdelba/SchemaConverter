package com.bassboy.controllers;

import com.bassboy.common.ConfigProp;
import com.bassboy.configuration.LinkedinTokenResponseConverter;
import com.bassboy.models.FormModel;
import com.bassboy.schemaevolver.InvalidEntryException;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.services.SchemaEvolverUserRepository;
import com.bassboy.services.SchemaResourceManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

@Controller
public class SchemaEvolverController {

    @Autowired
    private OAuth2AccessTokenResponseClient accessTokenResponseClient;

    @Autowired
    private SchemaResourceManager srm;

    @Autowired
    SchemaEvolverUserRepository repo;

    @RequestMapping("/")
    public String welcome() {
        return "index";
    }

    @RequestMapping(value="form")
    public String schemaConversionForm(Model model, Principal principal) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root =  mapper.readTree(mapper.writeValueAsString(principal));
        DocumentContext documentContext = JsonPath.parse(root.toString());
        String username;

        if(root.has("authorizedClientRegistrationId")) {
            if(documentContext.read("$.authorizedClientRegistrationId").toString().toLowerCase()
                    .equals("linkedin"))
                username = documentContext.read("$.authorities[0].attributes.localizedFirstName")
                        + " " + documentContext.read("$.authorities[0].attributes.localizedLastName");
            else
                username = documentContext.read("$.authorities[0].attributes.name");
        }
        else
            username = documentContext.read("$.name");

        model.addAttribute("username", username);
        model.addAttribute("formModel", new FormModel());
        return "form";
    }

    @RequestMapping(value="conversion",method = {RequestMethod.POST})
    public String schemaConversionLoading(@ModelAttribute("formModel") FormModel formModel, ModelMap model, Principal principal) throws IOException, SchemaEvolverException, InvalidEntryException {

        // Added wait time for UX
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        srm = new SchemaResourceManager(formModel);
        srm.init();
        srm.runConversion();
        model.remove("formModel");
        model.addAttribute("srm", srm);
        model.addAttribute("username",principal.getName());
//        ModelAndView modelAndView = new ModelAndView("download");
//        modelAndView.addObject("downloadFormat", scv.getDownloadFormat());
        return "complete";
    }

    @RequestMapping(path = "/download/{downloadFormat}", method = RequestMethod.GET)
    public void download(@PathVariable String downloadFormat, HttpServletResponse response, ModelMap model) throws IOException {
        response.setContentType("application/zip");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        response.setHeader("Content-Disposition", "attachment; filename=SchemaConverterResults_" + sdf.format(timestamp) + ".zip");
        try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
            srm.download(zippedOut);
        }//if try and fail, zippedOut is closed
    }

}
