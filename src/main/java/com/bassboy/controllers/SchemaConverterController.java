package com.bassboy.controllers;

import com.bassboy.models.SchemaConverterModel;
import com.bassboy.schemaconversion.SchemaConverterException;
import com.bassboy.services.SchemaResourceManager;
import com.bassboy.utils.ConfigProp;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class SchemaConverterController implements ErrorController {

    private SchemaResourceManager srm;

    @RequestMapping(value = "/", method = {RequestMethod.GET})
    public String welcome() {
        return "index";
    }

    @RequestMapping(value="form",method = {RequestMethod.POST})
    public String schemaConversionForm(Model model) throws IOException {
        model.addAttribute("scm", new SchemaConverterModel());
        return "form";
    }

    @RequestMapping(value="run_conversion",method = {RequestMethod.POST})
    public String schemaConversionLoading(@ModelAttribute("scm") SchemaConverterModel scm, ModelMap model) throws IOException, SchemaConverterException {

        // Added wait time for UX
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        srm = new SchemaResourceManager(scm);

        srm.init();
        srm.runConversion();
        model.remove("scm");
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

        SchemaConverterModel scv = new SchemaConverterModel(oldJsonFiles,oldSchemaFile,newSchemaFile,renamedFile,
                oldJsonText,oldSchemaText,newSchemaText,renamedText,downloadFormat);

        SchemaResourceManager srm = new SchemaResourceManager(scv);
        srm.init();
    }


}
