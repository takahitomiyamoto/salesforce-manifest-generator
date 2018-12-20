package com.smg.util;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.smg.util.CommonUtils;
import java.io.IOException;
import org.json.JSONObject;

public class LoginUtils {

    private static final String CREDENTIALS_FILE = CommonUtils.CREDENTIALS_FILE;
    private static final String FILE_ENCODING_UTF8 = CommonUtils.FILE_ENCODING_UTF8;

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
        System.out.println("userName: " + loginResult.getUserInfo().getUserName());
        System.out.println("sessionId: " + loginResult.getSessionId());

        final MetadataConnection metadataConnection = createMetadataConnection(loginResult);
        return metadataConnection;
    }

    private static LoginResult loginToSalesforce(final String username, final String password, final String authEndPoint, final String proxyHost, final int proxyPort)
      throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(authEndPoint);
        config.setServiceEndpoint(authEndPoint);
        config.setManualLogin(true);
        if (!"".equals(proxyHost) && 0 != proxyPort) {
            config.setProxy(proxyHost, proxyPort);
        }

        final PartnerConnection partnerConnection = new PartnerConnection(config);
        return partnerConnection.login(username, password);
    }

    private static MetadataConnection createMetadataConnection(final LoginResult loginResult)
      throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setServiceEndpoint(loginResult.getMetadataServerUrl());
        config.setSessionId(loginResult.getSessionId());

        final MetadataConnection metadataConnection = new MetadataConnection(config);
        return metadataConnection;
    }

}