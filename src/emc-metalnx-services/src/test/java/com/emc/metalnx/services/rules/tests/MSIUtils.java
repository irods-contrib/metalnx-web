/*
 * Copyright (c) 2015-2017, Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public List<String> getMsiList() {
        return msiList;
    }

    public List<String> getMlxMSIList() {
        return mlxMSIList;
    }

    public List<String> getIrods41XMSIs() {
        return irods41XMSIs;
    }

    public List<String> getIrods420MSIs() {
        return irods420MSIs;
    }

    public List<String> getOtherMSIs() { return otherMSIList; }
}
