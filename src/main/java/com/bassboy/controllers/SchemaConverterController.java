package com.bassboy.controllers;

import com.bassboy.models.SchemaConverterModel;
import com.bassboy.schemaconversion.SchemaConverterException;
import com.bassboy.schemaconversion.SchemaObject;
import com.bassboy.utils.GeneralUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
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

    @RequestMapping(value="runConversion",method = {RequestMethod.POST})
    public void schemaConversionLoading() throws IOException, SchemaConverterException {





        // DEBUG
        String oldSchemaFile = "schema1.avsc";
        String latestSchemaFile = "schema2.avsc";
        List<String> oldJsonFiles = new ArrayList<>();
        oldJsonFiles.add("record.json");
        //renamedFieldWithNoAliasDelimiterSeperated list format: latestName=oldName
        String renamedFieldWithNoAliasDelimiterSeperated = "SchoolFriend=SchoolFriends\nemailAddress=email";
        HashMap<String,String> renamedWithNoAliasMap = GeneralUtils.getMapFromNewlineSeperatedString(renamedFieldWithNoAliasDelimiterSeperated);


        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SchemaConverterModel scModel = new SchemaConverterModel(oldSchemaFile,latestSchemaFile,oldJsonFiles,renamedWithNoAliasMap);
        scModel.matchToSchema();





    }

    @RequestMapping(value="error")
    public String handleError() {
        return "error-500";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
