package com.emc.metalnx.services.rules.tests;

import java.util.ArrayList;
import java.util.List;

/**
 * Microservices utils for testing.
 */
public class MSIUtils {

    private static String msiVersion;
    private List<String> msiList, mlxMSIList, irods41XMSIs, irods420MSIs, otherMSIList;

    public MSIUtils() {
        msiVersion = "1.1.0";

        msiList = new ArrayList<>();
        mlxMSIList = new ArrayList<>();
        irods41XMSIs = new ArrayList<>();
        irods420MSIs = new ArrayList<>();
        otherMSIList = new ArrayList<>();

        mlxMSIList.add("libmsiget_illumina_meta.so");
        mlxMSIList.add("libmsiobjget_microservices.so");
        mlxMSIList.add("libmsiobjget_version.so");
        mlxMSIList.add("libmsiobjjpeg_extract.so");
        mlxMSIList.add("libmsiobjput_mdbam.so");
        mlxMSIList.add("libmsiobjput_mdbam.so");
        mlxMSIList.add("libmsiobjput_mdmanifest.so");
        mlxMSIList.add("libmsiobjput_mdvcf.so");
        mlxMSIList.add("libmsiobjput_populate.so");

        irods420MSIs.add("libmsisync_to_archive.so");
        irods420MSIs.add("libmsi_update_unixfilesystem_resource_free_space.so");

        irods41XMSIs.addAll(irods420MSIs);
        irods41XMSIs.add("libmsiobjput_http.so");
        irods41XMSIs.add("libmsiobjput_irods.so");
        irods41XMSIs.add("libmsiobjget_irods.so");
        irods41XMSIs.add("libmsiobjget_http.so");
        irods41XMSIs.add("libmsiobjput_slink.so");
        irods41XMSIs.add("libmsiobjget_slink.so");

        otherMSIList.add("libmsitest_other1.so");
        otherMSIList.add("libmsitest_other2.so");

        msiList.addAll(mlxMSIList);
        msiList.addAll(irods41XMSIs);
        msiList.addAll(otherMSIList);
    }

    public static String getMsiVersion() {
        return msiVersion;
    }

    public static void setMsiVersion(String msiVersion) {
        MSIUtils.msiVersion = msiVersion;
    }

    public List<String> getMsiList() {
        return msiList;
    }

    public void setMsiList(List<String> msiList) {
        this.msiList = msiList;
    }

    public List<String> getMlxMSIList() {
        return mlxMSIList;
    }

    public void setMlxMSIList(List<String> mlxMSIList) {
        this.mlxMSIList = mlxMSIList;
    }

    public List<String> getIrods41XMSIs() {
        return irods41XMSIs;
    }

    public void setIrods41XMSIs(List<String> irods41XMSIs) {
        this.irods41XMSIs = irods41XMSIs;
    }

    public List<String> getIrods420MSIs() {
        return irods420MSIs;
    }

    public void setIrods420MSIs(List<String> irods420MSIs) {
        this.irods420MSIs = irods420MSIs;
    }

    public List<String> getOtherMSIList() {
        return otherMSIList;
    }

    public void setOtherMSIList(List<String> otherMSIList) {
        this.otherMSIList = otherMSIList;
    }
}
