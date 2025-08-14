 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine.util;

import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ServerUtil {

    private static final Logger logger = LogManager.getLogger(ServerUtil.class);

    /**
     * Get machine information such as disk, memory, and cpu status
     *
     * @param hostname
     *            host that we want to get the information from
     * @param infoType
     *            disk, memory, cpu, irodsstatus, irodslogs
     * @return JSON
     *         that contains the status of a machine based on the type
     */
    static public String getMachineInformation(String hostname, String port,
            ServerRequestInfoType infoType, int timeout) {
        try {

            String urlString = "http://" + hostname + ":" + port + "/" + infoType.toString();

            logger.info("Making HTTP request to {}", urlString);
            URL url = new URL(urlString);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(timeout);

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                        httpURLConnection.getInputStream()));

                StringBuffer response = new StringBuffer();
                String temp = "";
                while ((temp = bufferedReader.readLine()) != null) {
                    response.append(temp);
                }

                bufferedReader.close();
                return response.toString();
            }
        }
        catch (IOException e) {
            logger.error("Could not get machine information", e.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Creates a DataGridServer instance based on JSON response from RMD.
     *
     * @param jsonResponse response from external service in JSON
     * @return dataGridServer
     */
    static public void populateDataGridServerStatus(String jsonResponse, DataGridServer dataGridServer) {

        // RMD package isn't available
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            dataGridServer.setMachineStatus(ServerRequestInfoType.WARNING_STATUS.toString());
            dataGridServer.setDataGridStatus(ServerRequestInfoType.WARNING_STATUS.toString());
            dataGridServer.setMemoryStatus(ServerRequestInfoType.WARNING_STATUS.toString());
            dataGridServer.setDiskStatus(ServerRequestInfoType.WARNING_STATUS.toString());
            dataGridServer.setRmdPackageRunning(false);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonResponse);
            dataGridServer.setMachineStatus(json.get("server").textValue());
            dataGridServer.setDataGridStatus(json.get("irods_server").textValue());
            dataGridServer.setMemoryStatus(json.get("memory").textValue());
            dataGridServer.setDiskStatus(json.get("disk").textValue());
            dataGridServer.setRmdPackageRunning(true);
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse server status JSON response", e);
        }
        catch (IOException e) {
            logger.error("Could not parse server status JSON response", e);
        }
    }

    static public HashMap<String, String> getNFSMountMap(String mountInfoJSON) {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonArray = mapper.readTree(mountInfoJSON);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonNode jsonObj = jsonArray.get(i);
                String type = jsonObj.get("type").textValue();

                if (type.compareTo("nfs") == 0) {
                    String localPath = jsonObj.get("local_path").textValue();
                    String remoteIP = jsonObj.get("remote_ip").textValue();
                    hashMap.put(localPath, remoteIP);
                }
            }

        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse server status JSON response", e);
        }
        catch (IOException e) {
            logger.error("Could not parse server status JSON response", e);
        }

        return hashMap;
    }

    /**
     * Gets DataGridServer RMD package information: release number and version.
     *
     * @param jsonResponse
     * @return dataGridServer
     */
    static public void setDataGridServerRMDInfo(String jsonResponse, DataGridServer dataGridServer) {

        // RMD package isn't available
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            dataGridServer.setRmdPackageRelease(null);
            dataGridServer.setRmdPackageVersion(null);
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonResponse);
            dataGridServer.setRmdPackageRelease(json.get("release").textValue());
            dataGridServer.setRmdPackageVersion(json.get("version").textValue());
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse server status JSON response", e);
        }
        catch (IOException e) {
            logger.error("Could not parse server status JSON response", e);
        }
    }

}
