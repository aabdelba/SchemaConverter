package com.bassboy.controllers;

import com.bassboy.models.FormModel;
import com.bassboy.models.User;
import com.bassboy.schemaevolver.InvalidEntryException;
import com.bassboy.schemaevolver.SchemaEvolverException;
import com.bassboy.secureapp.UserRepository;
import com.bassboy.services.SchemaResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

@Controller
public class SchemaEvolverController implements ErrorController {

    @Autowired
    private SchemaResourceManager srm;

    @Autowired
    UserRepository repo;

    @RequestMapping("/")
    public String welcome() {
        return "index";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            // get ${SPRING_SECURITY_LAST_EXCEPTION.message
            BadCredentialsException ex = (BadCredentialsException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
        }
        model.addAttribute("errorMessage", errorMessage);
        return "login";
    }

    @RequestMapping("/logout")
    public String logout() {
        return "index";
    }

    @RequestMapping(value="form")
    public String schemaConversionForm(Model model, Principal principal) throws IOException {

        model.addAttribute("username", principal.getName());
        model.addAttribute("formModel", new FormModel());
        return "form";
    }

    @RequestMapping("/user")
    @ResponseBody
    public Principal user (Principal principal){
        return principal;
    }

    @RequestMapping(value="conversion",method = {RequestMethod.POST})
    public String schemaConversionLoading(@ModelAttribute("formModel") FormModel formModel, ModelMap model) throws IOException, SchemaEvolverException, InvalidEntryException {

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

    @RequestMapping(value="error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String errorTitle;

        if (status != null) {

            switch(status.toString()){
                case("500"):
                    errorTitle = " - Internal server error, our engineers are working on it";
                    break;
                case("400"):
                    errorTitle = " - Bad request";
                    break;
                case("404"):
                    errorTitle = " - Not found";
                    break;
                case("405"):
                    errorTitle = " - Method not allowed";
                    break;
                case("422"):
                    errorTitle = " - Unprocessable entity";
                    break;
                default:
                    errorTitle = "";
                    break;
            }
            model.addAttribute("status",status);
            model.addAttribute("errorTitle",errorTitle);
            model.addAttribute("message",message);

        }
        return "error";
    }


    @Override
    public String getErrorPath() {
        return "/error";
    }

    // DEBUG
    public static void main(String[] args) throws IOException {

        MultipartFile[] oldJsonFiles = new MultipartFile[0];
        MultipartFile oldSchemaFile = null;
        MultipartFile newSchemaFile = null;
        MultipartFile renamedFile = null;

        String oldJsonText = "asdfasfad ;;; asdfwefer ;;; awfasdfver ;;;";
        String oldSchemaText = "asdfasdfawefwerf";
        String newSchemaText = "asdfasdfawefwerf";
        String renamedText = "asdfasdfawefwerf=adfwedcf";

        String downloadFormat = "json";

        FormModel scv = new FormModel(oldJsonFiles,oldSchemaFile,newSchemaFile,renamedFile,
                oldJsonText,oldSchemaText,newSchemaText,renamedText,downloadFormat);

        SchemaResourceManager srm = new SchemaResourceManager(scv);
        srm.init();
    }


}
