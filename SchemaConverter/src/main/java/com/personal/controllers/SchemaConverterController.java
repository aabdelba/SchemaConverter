package com.personal.controllers;

import com.personal.models.SchemaConverterModel;
import com.personal.schemaConversion.SchemaConverterException;
import com.personal.services.SchemaConverterService;
import com.personal.utils.ConfigProp;
import com.personal.utils.GeneralUtils;
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
        return "welcome";
    }

    @RequestMapping(value="form",method = {RequestMethod.GET, RequestMethod.POST})
    public String schemaConversionForm() {
        return "schemaConversionForm";
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
