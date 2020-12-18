package gov.cdc.usds.simplereport.api.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cdc.usds.simplereport.service.DataHubUploaderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * Created by tomn on 11Dec2020
 */
@RestController
@RequestMapping(value = "/export")
@Validated
public class DataHubUploadController {

    private final DataHubUploaderService _hubuploadservice;

    @Value("${simple-report.insecure-cookies:false}")
    private boolean _insecurecookies;

    public DataHubUploadController(DataHubUploaderService us) {
        this._hubuploadservice = us;
    }

    @GetMapping(value = "/uploadTestEvent", produces = MediaType.APPLICATION_JSON_VALUE)
    public String uploadTestEventCSVToDataHub(@RequestParam String apikey,
                                              @RequestParam(defaultValue = "") String startupdateby
    ) throws IOException {
        Map<String, String> result = _hubuploadservice.uploadTestEventCVSToDataHub(apikey, startupdateby);
        ObjectMapper mapperObj = new ObjectMapper();
        return mapperObj.writeValueAsString(result);
    }

    @GetMapping(value = "/testEvent", produces = {"text/csv"})
    public ResponseEntity<?> exportTestEventCSV(HttpServletResponse response,
                                                @RequestParam(defaultValue = "") String startupdateby,
                                                @CookieValue(value = "csvsavedstartupby", defaultValue = "")
                                                        String startupdatebyCookie) throws IOException {
        if (startupdateby.length() == 0 && startupdatebyCookie.length() > 0) {
            // if the value doesn't come from the url and it's available in the cookie, use that.
            startupdateby = startupdatebyCookie;
        } else if (startupdateby.compareToIgnoreCase("all") == 0) {
            startupdateby = DataHubUploaderService.EARLIEST_TIMESTAMP;
        }

        response.setContentType("text/csv");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());
        // we want to set the next timestamp in the cookie. But cookie headers must be set before
        // we start writing back out the html body. So preload the csv so we can call getNextTimestamp()
        String csvString = this._hubuploadservice.creatTestCVSForDataHub(startupdateby);

        String nexttimestamp = this._hubuploadservice.getNextTimestamp();

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=testEvents_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);
        if (nexttimestamp.length() > 0) {  // if there were no matches then this will be empty.
            Cookie cookie = new Cookie("csvsavedstartupby", nexttimestamp);
            final int SECONDS_TO_EXPIRE_COOKIE = 60 * 60 * 24 * 90;  // 90 days to refresh
            // if we setSecure(true) for localhost, then setting cookie fails.
            cookie.setHttpOnly(this._insecurecookies);
            cookie.setSecure(!this._insecurecookies);
            cookie.setMaxAge(SECONDS_TO_EXPIRE_COOKIE);
            cookie.setPath("/");
            response.addCookie(cookie); // NOSONAR lgtm [java/insecure-cookie]
        }
        response.getWriter().print(csvString);
        return ResponseEntity.accepted().build();
    }
}
