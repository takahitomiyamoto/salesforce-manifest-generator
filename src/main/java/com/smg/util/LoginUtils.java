package com.smg.util;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.smg.util.CommonUtils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class LoginUtils {

    private static final String CREDENTIALS_FILE = CommonUtils.CREDENTIALS_FILE;
    private static final String FILE_ENCODING_UTF8 = CommonUtils.FILE_ENCODING_UTF8;
    private static SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static MetadataConnection login()
      throws ConnectionException, IOException {
        final JSONObject jsonObj = new JSONObject(CommonUtils.readAllLine(CREDENTIALS_FILE, FILE_ENCODING_UTF8));

        final String username = CommonUtils.getUsername(jsonObj);
        final String password = CommonUtils.getPassword(jsonObj);
        final String orgType = CommonUtils.getOrgType(jsonObj);
        final Double apiVersion = CommonUtils.getApiVersion(jsonObj);
        final String authEndPoint = CommonUtils.getAuthEndPoint(orgType, apiVersion);
        final String proxyHost = CommonUtils.getProxyHost(jsonObj);
        final int proxyPort = CommonUtils.getProxyPort(jsonObj);

        final LoginResult loginResult = loginToSalesforce(username, password, authEndPoint, proxyHost, proxyPort);

        System.out.print("[" + simpleDate.format(new Date()) + "] ");
        System.out.println("successfully login: " + loginResult.getUserInfo().getUserName());

        final MetadataConnection metadataConnection = createMetadataConnection(loginResult, proxyHost, proxyPort);
        return metadataConnection;
    }

    private static ConnectorConfig setProxy(ConnectorConfig config, String proxyHost, int proxyPort) {
        System.out.print("[" + simpleDate.format(new Date()) + "] ");
        if (!"".equals(proxyHost) && 0 != proxyPort) {
            config.setProxy(proxyHost, proxyPort);
            System.out.println("try to set the proxy option...");
        } else {
            System.out.println("did not set the proxy option...");
        }
        return config;
    }

    private static LoginResult loginToSalesforce(String username, String password, String authEndPoint, String proxyHost, int proxyPort)
      throws ConnectionException {
        ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(authEndPoint);
        config.setServiceEndpoint(authEndPoint);
        config.setManualLogin(true);
        config = setProxy(config, proxyHost, proxyPort);

        final PartnerConnection partnerConnection = new PartnerConnection(config);
        return partnerConnection.login(username, password);
    }

    private static MetadataConnection createMetadataConnection(LoginResult loginResult, String proxyHost, int proxyPort)
      throws ConnectionException {
        ConnectorConfig config = new ConnectorConfig();
        config.setServiceEndpoint(loginResult.getMetadataServerUrl());
        config.setSessionId(loginResult.getSessionId());
        config = setProxy(config, proxyHost, proxyPort);

        final MetadataConnection metadataConnection = new MetadataConnection(config);
        return metadataConnection;
    }

}