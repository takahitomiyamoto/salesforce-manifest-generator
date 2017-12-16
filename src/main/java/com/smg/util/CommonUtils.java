package com.smg.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONObject;

public class CommonUtils {

    public static final String CREDENTIALS = "credentials";
    public static final String CREDENTIALS_FILE = "credentials.json";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ORG_TYPE = "orgType";
    public static final String API_VERSION = "apiVersion";
    public static final String PROPERTY_LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String HTTPS = "https://";
    public static final String SERVICE_SOAP_U = ".salesforce.com/services/Soap/u/";

    public static String getUsername(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        final String username = credential.getString(USERNAME);

        return username;
    }

    public static String getPassword(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        final String password = credential.getString(PASSWORD);

        return password;
    }

    public static String getOrgType(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        final String orgType = credential.getString(ORG_TYPE);

        return orgType;
    }

    public static String getAuthEndPoint(final String orgType, final Double apiVersion) {
        final String authEndPoint = CommonUtils.HTTPS + orgType + CommonUtils.SERVICE_SOAP_U + apiVersion;

        return authEndPoint;
    }

    public static Double getApiVersion(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        final Double apiVersion = credential.getDouble(API_VERSION);

        return apiVersion;
    }

    public static String readAllLine(final String path)
      throws IOException {
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();

        while (line != null){
            builder.append(line);
            builder.append(PROPERTY_LINE_SEPARATOR);
            line = reader.readLine();
        }
        reader.close();
        final String builderString = builder.toString();

        return builderString;
    }

}