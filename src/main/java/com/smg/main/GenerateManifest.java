package com.smg.main;

import com.sforce.soap.metadata.MetadataConnection;
import com.smg.util.MetadataUtils;
import com.smg.util.LoginUtils;

public class GenerateManifest {

    public static void main(String[] args) {
        try {
            MetadataConnection metadataConnection = LoginUtils.login();
            MetadataUtils.listMetadata(metadataConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}