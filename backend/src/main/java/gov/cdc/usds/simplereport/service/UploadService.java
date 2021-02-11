package gov.cdc.usds.simplereport.service;

import static gov.cdc.usds.simplereport.api.Translators.parseEmail;
import static gov.cdc.usds.simplereport.api.Translators.parseEthnicity;
import static gov.cdc.usds.simplereport.api.Translators.parseGender;
import static gov.cdc.usds.simplereport.api.Translators.parsePersonRole;
import static gov.cdc.usds.simplereport.api.Translators.parsePhoneNumber;
import static gov.cdc.usds.simplereport.api.Translators.parseRaceDisplayValue;
import static gov.cdc.usds.simplereport.api.Translators.parseState;
import static gov.cdc.usds.simplereport.api.Translators.parseString;
import static gov.cdc.usds.simplereport.api.Translators.parseUserShortDate;
import static gov.cdc.usds.simplereport.api.Translators.parseUUID;
import static gov.cdc.usds.simplereport.api.Translators.parseYesNo;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.cdc.usds.simplereport.config.AuthorizationConfiguration;
import gov.cdc.usds.simplereport.api.model.errors.IllegalGraphqlArgumentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by nickrobison on 11/21/20
 */
@Service
@Transactional
public class UploadService {
    private static final CsvSchema PERSON_SCHEMA = personSchema();
    private static final String FACILITY_ID = "facilityId";

    private final PersonService _ps;

    public UploadService(PersonService ps) {
        this._ps = ps;
    }

    private MappingIterator<Map<String, String>> getIteratorForCsv(InputStream csvStream) throws IllegalGraphqlArgumentException {
        try {
            return new CsvMapper()
                .enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
                .readerFor(Map.class)
                .with(PERSON_SCHEMA)
                .readValues(csvStream);
        } catch (IOException e) {
            throw new IllegalGraphqlArgumentException(e.getMessage());
        }
    }

    private Map<String, String> getNextRow(MappingIterator<Map<String, String>> valueIterator) throws IllegalGraphqlArgumentException {
        try {
            return valueIterator.next();
        } catch (RuntimeJsonMappingException e) {
            throw new IllegalGraphqlArgumentException(e.getMessage());
        }
    }

    public String getRow(Map<String, String> row, String name, boolean isRequired) {
        String value = row.get(name);
        if (!isRequired) {
            return value;
        }
        if(value == null || value.trim().isEmpty()) {
            throw new IllegalGraphqlArgumentException(name + " is required.");
        }
        return value;
    }

    @AuthorizationConfiguration.RequireGlobalAdminUser
    public String processPersonCSV(InputStream csvStream) throws IllegalGraphqlArgumentException {
        final MappingIterator<Map<String, String>> valueIterator = getIteratorForCsv(csvStream);

        // Since the CSV parser won't fail when give a single string, we simple check to see if it has any parsed values
        // If not, we throw an error assuming the user didn't actually want to submit something empty.
        if (!valueIterator.hasNext()) {
            throw new IllegalGraphqlArgumentException("Empty or invalid CSV submitted");
        }

        int rowNumber = 0;
        while (valueIterator.hasNext()) {
            final Map<String, String> row = getNextRow(valueIterator);
            rowNumber++;
            try {
                _ps.addPatient(
                    parseUUID(getRow(row, FACILITY_ID, false)),
                    null, // lookupID. this field is deprecated
                    parseString(getRow(row, "FirstName", true)),
                    parseString(getRow(row, "MiddleName", false)),
                    parseString(getRow(row, "LastName", true)),
                    parseString(getRow(row, "Suffix", false)),
                    parseUserShortDate(getRow(row, "DOB", true)),
                    parseString(getRow(row, "Street", true)),
                    parseString(getRow(row, "Street2", false)),
                    parseString(getRow(row, "City", false)),
                    parseState(getRow(row, "State", true)),
                    parseString(getRow(row, "ZipCode", true)),
                    parsePhoneNumber(getRow(row, "PhoneNumber", true)),
                    parsePersonRole(getRow(row, "Role", false)),
                    parseEmail(getRow(row, "Email", false)),
                    parseString(getRow(row, "County", false)),
                    parseRaceDisplayValue(getRow(row, "Race", false)),
                    parseEthnicity(getRow(row, "Ethnicity", false)),
                    parseGender(getRow(row, "biologicalSex", false)),
                    parseYesNo(getRow(row, "residentCongregateSetting", true)),
                    parseYesNo(getRow(row, "employedInHealthcare", true))
                );
            } catch (IllegalGraphqlArgumentException e) {
                throw new IllegalGraphqlArgumentException("Error on row "+ rowNumber+ "; " + e.getMessage());
            }
        }
        return "Successfully uploaded " + rowNumber + " record(s)";
    }

    private static CsvSchema personSchema() {
        return CsvSchema.builder()
                .addColumn("FirstName", CsvSchema.ColumnType.STRING)
                .addColumn("LastName", CsvSchema.ColumnType.STRING)
                .addColumn("MiddleName", CsvSchema.ColumnType.STRING)
                .addColumn("Suffix", CsvSchema.ColumnType.STRING)
                .addColumn("Race", CsvSchema.ColumnType.STRING)
                .addColumn("DOB", CsvSchema.ColumnType.STRING)
                .addColumn("biologicalSex", CsvSchema.ColumnType.STRING)
                .addColumn("Ethnicity", CsvSchema.ColumnType.STRING)
                .addColumn("Street", CsvSchema.ColumnType.STRING)
                .addColumn("Street2", CsvSchema.ColumnType.STRING)
                .addColumn("City", CsvSchema.ColumnType.STRING)
                .addColumn("County", CsvSchema.ColumnType.STRING)
                .addColumn("State", CsvSchema.ColumnType.STRING)
                .addColumn("ZipCode", CsvSchema.ColumnType.STRING)
                .addColumn("PhoneNumber", CsvSchema.ColumnType.STRING)
                .addColumn("employedInHealthcare", CsvSchema.ColumnType.STRING)
                .addColumn("residentCongregateSetting", CsvSchema.ColumnType.STRING)
                .addColumn("Role", CsvSchema.ColumnType.STRING)
                .addColumn("Email", CsvSchema.ColumnType.STRING)
                .addColumn(FACILITY_ID, CsvSchema.ColumnType.STRING)
                .setUseHeader(true)
                .build();
    }

}
