 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridZone;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

import java.util.List;

public interface ZoneService {

    /**
     * Finds all zones existing in the data grid
     *
     * @return List of zones
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridZone> findAll() throws DataGridConnectionRefusedException;
}
