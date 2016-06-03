/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.core.domain.utils;

/**
 * Helper class for common operations with core classes.
 *
 */
public class DataGridCoreUtils {

    /**
     * Gets the icon type that will be shown on the UI.
     *
     * @param filePath
     *            path to the file
     * @return the icon type as String
     */
    public static String getIconToDisplay(String filePath) {

        String icon = "";
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
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath) {

        if (filePath.lastIndexOf(".") != -1 && filePath.lastIndexOf(".") != 0) {
            return filePath.substring(filePath.lastIndexOf(".") + 1);
        }

        else
            return "unknown";

    }

}
