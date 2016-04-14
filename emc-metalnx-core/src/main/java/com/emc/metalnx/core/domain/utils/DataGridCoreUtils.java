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
