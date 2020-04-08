package com.bassboy.controllers;

import com.bassboy.models.SchemaConverterModel;
import com.bassboy.schemaConversion.SchemaConverterException;
import com.bassboy.services.SchemaConverterService;
import com.bassboy.utils.ConfigProp;
import com.bassboy.utils.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class SchemaConverterController {

    @RequestMapping(value = "/", method = {RequestMethod.GET})
    public String welcome() {
        return "index";
    }

    @RequestMapping(value="form",method = {RequestMethod.POST})
    public String schemaConversionForm() {
        return "form";
    }

    @RequestMapping(value="loading",method = {RequestMethod.POST})
    public String schemaConversionLoading() {
        System.out.println("DEBUG");return "loading";
    }
    


    public void runConversion() throws IOException, SchemaConverterException {

        // TODO: this is all to be done in the controller
        ConfigProp configProp = ConfigProp.getInstance();
        String inputDir = configProp.getProperty("input.dir");
        String oldSchemaFile = "schema1.avsc";
        String latestSchemaFile = "schema2.avsc";
        List<String> oldJsonFiles = new ArrayList<>();
        oldJsonFiles.add("record.json");
        //renamedFieldWithNoAliasCommaSeperated list format: latestName=oldName
        String renamedFieldWithNoAliasDelimiterSeperated = "SchoolFriend=SchoolFriends\nemailAddress=email";
        HashMap<String,String> renamedWithNoAliasMap = GeneralUtils.getMapFromNewlineSeperatedString(renamedFieldWithNoAliasDelimiterSeperated);
        SchemaConverterModel schemaConverterModel = new SchemaConverterModel(oldSchemaFile,latestSchemaFile,oldJsonFiles,renamedWithNoAliasMap);

        // run the conversion
        SchemaConverterService.runConversion(oldSchemaFile,latestSchemaFile,oldJsonFiles,renamedWithNoAliasMap);

    }

}
