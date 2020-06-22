Steps for using the Schema Evolver Form with demo files:

    1. Provide one or both of 'demo_record1.json' and 'demo_record2.json' in the old json section

    2. Provide 'demo_oldSchema.avsc' in the old avsc schema section

    3. Provide 'demo_newSchema.avsc' in the new avsc schema section

    4. Renamed avro fields is optional, but since this demo has renamed fields without an alias,
        provide 'demo_renamedFields.txt' in this section. If this is not done, the result files
        will have the prefix "ERROR_" which means any records that encountered a conversion issue
        has not been converted due to either invalid inputs or unimplemented features

    5. Select the file format you desire in the file download format section

    6. Once the conversion is complete, if any one of the provided records encountered a conversion
        error, A warning symbol should appear to indicate that the conversion is complete with errors.
        Any records with the prefix "ERROR_" have encountered an error, otherwise they have been
        converted successfully.
        If no errors were encountered, a message saying "Conversion complete!" should appear meaning
        all the results are valid
