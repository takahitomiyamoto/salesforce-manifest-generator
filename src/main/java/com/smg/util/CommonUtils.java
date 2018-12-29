package com.smg.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONObject;

public class CommonUtils {

    private static final String CREDENTIALS = "credentials";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PROXY_HOST = "proxyHost";
    private static final String PROXY_PORT = "proxyPort";
    private static final String ORG_TYPE = "orgType";
    private static final String API_VERSION = "apiVersion";
    private static final String EXCEPT_MANAGED_PACKAGE = "exceptManagedPackage";
    private static final String EXCEPT_UNMANAGED_PACKAGE = "exceptUnmanagedPackage";
    private static final String PROPERTY_LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String HTTPS = "https://";
    private static final String SERVICE_SOAP_U = ".salesforce.com/services/Soap/u/";
    private static final String OS = "os";
    private static final String OS_WIN = "win";
    private static final String OS_MAC = "mac";
    private static final String FILE_ENCODING_SJIS = "Shift_JIS";
    public static final String CREDENTIALS_FILE = "credentials.json";
    public static final String FILE_ENCODING_UTF8 = "UTF-8";

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

    public static Double getApiVersion(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        final Double apiVersion = credential.getDouble(API_VERSION);

        return apiVersion;
    }

    public static String getAuthEndPoint(final String orgType, final Double apiVersion) {
        final String authEndPoint = CommonUtils.HTTPS + orgType + CommonUtils.SERVICE_SOAP_U + apiVersion;

        return authEndPoint;
    }

    public static String getProxyHost(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        if (credential.has(PROXY_HOST)) {
            return credential.getString(PROXY_HOST);
        } else {
            return "";
        }
    }

    public static int getProxyPort(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        if (credential.has(PROXY_PORT)) {
            return credential.getInt(PROXY_PORT);
        } else {
            return 0;
        }
    }

    public static Boolean getExceptManagedPackage(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        if (credential.has(EXCEPT_MANAGED_PACKAGE)) {
            return credential.getBoolean(EXCEPT_MANAGED_PACKAGE);
        } else {
            return false;
        }
    }

    public static Boolean getExceptUnmanagedPackage(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        if (credential.has(EXCEPT_UNMANAGED_PACKAGE)) {
            return credential.getBoolean(EXCEPT_UNMANAGED_PACKAGE);
        } else {
            return false;
        }
    }

    public static String getOs(final JSONObject jsonObj) {
        final JSONObject credential = jsonObj.getJSONObject(CREDENTIALS);
        final String os = credential.getString(OS);

        return os;
    }

    public static String readAllLine(final String path, final String os)
      throws IOException {
        final StringBuilder builder = new StringBuilder();
        final FileInputStream input = new FileInputStream(path);
        final String fileEncoding = getFileEncoding(os);
        final InputStreamReader stream = new InputStreamReader(input, FILE_ENCODING_UTF8);
        final BufferedReader buffer = new BufferedReader(stream);
        final BufferedReader reader = buffer;
        String line = reader.readLine();

        while (line != null){
            byte[] bytes = line.getBytes();
            line = new String(bytes, fileEncoding);
            builder.append(line);
            builder.append(PROPERTY_LINE_SEPARATOR);
            line = reader.readLine();
        }
        reader.close();
        final String builderString = builder.toString();

        return builderString;
    }

    private static String getFileEncoding(final String os) {
        if (OS_WIN.equals(os)) {
            return FILE_ENCODING_SJIS;
        } else if (OS_MAC.equals(os)) {
            return FILE_ENCODING_UTF8;
        } else {
            return FILE_ENCODING_UTF8;
        }
    }

}