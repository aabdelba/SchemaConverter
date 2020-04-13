package com.bassboy.controllers;

import com.bassboy.models.SchemaConverterModel;
import com.bassboy.schemaconversion.SchemaConverterException;
import com.bassboy.schemaconversion.SchemaObject;
import com.bassboy.utils.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class SchemaConverterController implements ErrorController {

    @RequestMapping(value = "/", method = {RequestMethod.GET})
    public String welcome() {
        return "index";
    }

    @RequestMapping(value="form",method = {RequestMethod.POST})
    public String schemaConversionForm() {
        return "form";
    }



    @RequestMapping(value="debug",method = {RequestMethod.POST})
    @ResponseBody
    public String debugggg(@RequestParam("arg1") String arg1, @RequestParam("arg2") String arg2) {
        return        "<html>"+
                "<body>" +
                "DEBUG"+
                arg1+
                " \n"+
                arg2+
                "</body>" +
                "</html>";
    }



    public static File moveAndStoreFile(MultipartFile file, String name) throws IOException {
        String path = System.getProperty("user.dir")+"/src/main/resources/input/";
        String url = path+name;
        File fileToSave = new File(url);
        fileToSave.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileToSave);
        fos.write(file.getBytes());
        fos.close();
        return fileToSave;
    }




    @RequestMapping(value="run_conversion",method = {RequestMethod.POST})
    public String schemaConversionLoading(

            @RequestParam("oldJsonText") String oldJsonText, @RequestParam("oldJsonFiles") MultipartFile[] oldJsonFiles,
            @RequestParam("oldSchemaText") String oldSchemaText, @RequestParam("oldSchemaFile") MultipartFile oldSchemaFile,
            @RequestParam("newSchemaText") String newSchemaText, @RequestParam("newSchemaFile") MultipartFile newSchemaFile,
            @RequestParam("renamedText") String renamedText, @RequestParam("renamedFile") MultipartFile renamedFile,
            @RequestParam("renamedText") String fileFormat,
                                        Model model
                                        ) throws IOException, SchemaConverterException {
        String path = System.getProperty("user.dir")+"/src/main/resources/input/";
        for (MultipartFile oldJsonFile:oldJsonFiles) {
            if(!oldJsonFile.isEmpty()) oldJsonFile.transferTo(new File(path+oldJsonFile.getOriginalFilename()));
        }
        if(!oldSchemaFile.isEmpty()) oldSchemaFile.transferTo(new File(path+oldSchemaFile.getOriginalFilename()));
        if(!newSchemaFile.isEmpty()) newSchemaFile.transferTo(new File(path+newSchemaFile.getOriginalFilename()));
        if(!renamedFile.isEmpty()) renamedFile.transferTo(new File(path+renamedFile.getOriginalFilename()));


        System.out.println("-------------------------------------DEBUG");

//        // DEBUG
//        String oldSchemaFile = "schema1.avsc";
//        String latestSchemaFile = "schema2.avsc";
//        List<String> oldJsonFiles = new ArrayList<>();
//        oldJsonFiles.add("record.json");
//        //renamedFieldWithNoAliasDelimiterSeperated list format: latestName=oldName
//        String renamedFieldWithNoAliasDelimiterSeperated = "SchoolFriend=SchoolFriends\nemailAddress=email";
//        HashMap<String,String> renamedWithNoAliasMap = GeneralUtils.getMapFromNewlineSeperatedString(renamedFieldWithNoAliasDelimiterSeperated);


        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        SchemaConverterModel scModel = new SchemaConverterModel(oldSchemaFile,latestSchemaFile,oldJsonFiles,renamedWithNoAliasMap);
//        scModel.matchToSchema();

        return "download";
    }

    @RequestMapping(value="error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String errorTitle;

        if (status != null) {

            switch(status.toString()){
                case("500"):
                    errorTitle = "Internal server error, our engineers are working on it";
                    break;
                case("400"):
                    errorTitle = "Bad request";
                    break;
                case("404"):
                    errorTitle = "Not found";
                    break;
                case("405"):
                    errorTitle = "Method not allowed";
                    break;
                default:
                    errorTitle = "Our engineers are working on it";
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
}
