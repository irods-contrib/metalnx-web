 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import java.io.File;

/**
 * Interface for zipping files.
 */
public interface ZipService {
    /**
     * Zips a directory inside another.
     * @param directoryToPlaceZip directory where the zip file will be placed
     * @param directoryToZip directory to be zipped
     * @return File that represents a compressed zip
     */
    File createZip(File directoryToPlaceZip, File directoryToZip);
}
