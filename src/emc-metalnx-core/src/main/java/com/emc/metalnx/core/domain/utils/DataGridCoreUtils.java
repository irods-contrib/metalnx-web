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

package com.emc.metalnx.core.domain.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class for common operations with core classes.
 *
 */
public class DataGridCoreUtils {

    public static final String MSI_LIST_SEPARATOR = ",";

    /**
     * Finds the MSI API version currently supported.
     * @return String containing the MSI API version
     */
    public static String getAPIVersion(String version) {
        if(version == null || version.isEmpty()) return "";

        int end = version.indexOf('.');

        if (end < 0) end = version.length();

        return version.substring(0, end);
    }

    public static boolean isIllumina(String path) {
        return path.endsWith("_SSU.tar");
    }

    /**
     * Checks whether or not a given path refers to a BAM or CRAM file.
     * @param path file path
     * @return True, if file is BAM/CRAM. False, otherwise.
     */
    public static boolean isBamOrCram(String path) {
        return path.endsWith(".cram") || path.endsWith(".bam");
    }

    /**
     * Auxiliary method to determine wether a file is an image file
     *
     * @param path file path
     * @return bool True, if the given path is an image. False, otherwise.
     */
    public static boolean isImageFile(String path) {
        Set<String> extensions = new HashSet<>();
        extensions.add("png");
        extensions.add("PNG");
        extensions.add("jpg");
        extensions.add("JPG");
        extensions.add("jpeg");
        extensions.add("JPEG");
        extensions.add("bmp");
        extensions.add("BMP");

        String fileExtension = "";

        int i = path.lastIndexOf('.');
        if (i > 0) fileExtension = path.substring(i + 1);

        return extensions.contains(fileExtension);
    }

    /**
     * Auxiliary method to determine wether a file is a VCF file
     *
     * @param path file path
     * @return bool True, if the given path is a VCF file. False, otherwise.
     */
    public static boolean isVCFFile(String path) {
        Set<String> extensions = new HashSet<>();
        extensions.add("vcf");
        extensions.add("VCF");

        String fileExtension = "";

        int i = path.lastIndexOf('.');
        if (i > 0) {
            fileExtension = path.substring(i + 1);
        }

        return extensions.contains(fileExtension);
    }

    /**
     * Auxiliary method to determine whether a file is a VCF file
     *
     * @param path file path
     * @return bool True, if the given path is a manifest file. False, otherwise.
     */
    public static boolean isPrideXMLManifestFile(String path) {
        Set<String> extensions = new HashSet<>();
        extensions.add("xml");

        String fileExtension = "";

        int i = path.lastIndexOf('.');
        if (i > 0) {
            fileExtension = path.substring(i + 1);
        }

        return extensions.contains(fileExtension);
    }

    /**
     * Gets the icon type that will be shown on the UI.
     *
     * @param filePath path to the file
     * @return the icon type as String
     */
    public static String getIconToDisplay(String filePath) {

        String icon;
        String extension = getFileExtension(filePath);

        switch (extension) {

            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                icon = "fa fa-file-image-o";
                break;

            case "html":
            case "htm":
            case "xml":
            case "tex":
                icon = "fa fa-code";
                break;

            case "c":
            case "cpp":
            case "java":
            case "py":
                icon = "fa fa-file-code-o";
                break;

            case "docx":
            case "doc":
                icon = "fa fa-file-word-o";
                break;

            case "xlsx":
            case "xls":
                icon = "fa fa-file-excel-o";
                break;

            case "pptx":
            case "ppt":
                icon = "fa fa-file-powerpoint-o";
                break;

            case "pdf":
                icon = "fa fa-file-pdf-o";
                break;

            case "zip":
            case "rar":
                icon = "fa fa-file-archive-o";
                break;

            case "unknown":
                icon = "fa fa-folder folder-icon";
                break;

            default:
                icon = "fa fa-file";
                break;
        }

        return icon;

    }

    /**
     * Gets file extension based on its path
     *
     * @param filePath path to the file
     * @return file extension
     */
    private static String getFileExtension(String filePath) {

        if (filePath.lastIndexOf(".") != -1 && filePath.lastIndexOf(".") != 0) {
            return filePath.substring(filePath.lastIndexOf(".") + 1);
        }

        else
            return "unknown";

    }

    /**
     * Parses a raw list of MSIs coming from a rule.
     * @param msisAsString list of MSIs coming from the rule as a string
     * @return List of MSIs
     */
    public static List<String> getMSIsAsList(String msisAsString) {
        List<String> msis = new ArrayList<>();

        if(msisAsString != null && !msisAsString.isEmpty()) {
            for (String msi: msisAsString.split(MSI_LIST_SEPARATOR)) {
                String msiName = msi.trim();
                if (!msiName.isEmpty()) msis.add(msiName);
            }
        }

        return msis;
    }
}
