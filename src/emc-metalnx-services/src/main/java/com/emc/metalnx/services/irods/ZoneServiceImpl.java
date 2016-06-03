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

package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.ZoneAO;
import org.irods.jargon.core.pub.domain.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridZone;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.RemoteExecutionService;
import com.emc.metalnx.services.interfaces.ZoneService;

@Service
@Transactional
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    RemoteExecutionService remoteExecutionService;

    @Autowired
    private IRODSServices irodsServices;

    private static final Logger logger = LoggerFactory.getLogger(ZoneServiceImpl.class);

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
