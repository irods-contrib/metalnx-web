package com.emc.metalnx.services.interfaces;

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridZone;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface ZoneService {

    /**
     * Finds all zones existing in the data grid
     *
     * @return List of zones
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridZone> findAll() throws DataGridConnectionRefusedException;
}
