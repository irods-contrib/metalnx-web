 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridZone;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.RemoteExecutionService;
import com.emc.metalnx.services.interfaces.ZoneService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.ZoneAO;
import org.irods.jargon.core.pub.domain.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    RemoteExecutionService remoteExecutionService;

    @Autowired
    private IRODSServices irodsServices;

    private static final Logger logger = LogManager.getLogger(ZoneServiceImpl.class);

    @Override
    public List<DataGridZone> findAll() throws DataGridConnectionRefusedException {
        ZoneAO zoneAO = irodsServices.getZoneAO();
        List<DataGridZone> dataGridZones = null;

        try {
            List<Zone> zones = zoneAO.listZones();
            dataGridZones = new ArrayList<DataGridZone>();

            for (Zone zone : zones) {
                DataGridZone dataGridZone = new DataGridZone();
                dataGridZone.setId(Long.valueOf(zone.getZoneId()));
                dataGridZone.setName(zone.getZoneName());
                dataGridZone.setType(zone.getZoneType());
                dataGridZone.setCreateTime(zone.getZoneCreateTime());
                dataGridZone.setModifyTime(zone.getZoneModifyTime());
                dataGridZone.setConnectionString(zone.getZoneConnection());
                dataGridZone.setComment(zone.getZoneComment());

                // adding this current zone to the list
                dataGridZones.add(dataGridZone);
            }
        }
        catch (JargonException e) {
            logger.info("Could not find all zones ", e);
        }

        return dataGridZones;
    }
}
