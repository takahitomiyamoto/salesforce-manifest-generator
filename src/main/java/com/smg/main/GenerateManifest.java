package com.smg.main;

import com.sforce.soap.metadata.MetadataConnection;
import com.smg.util.LoginUtils;
import com.smg.service.MetadataService;

public class GenerateManifest {

    public static void main(String[] args) {
        try {
            MetadataConnection metadataConnection = LoginUtils.login();
            MetadataService service = new MetadataService(metadataConnection);
            service.generateManifest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}