package org.devgateway.geoph.services.geojson;

import org.devgateway.geoph.dao.LocationProjectStatsDao;
import org.geojson.Feature;

import static org.devgateway.geoph.core.constants.Constants.*;

/**
 * Created by sebas on 9/5/2016.
 */
public class LocationPointConverter extends AbstractConverter<LocationProjectStatsDao> {

    @Override
    public Feature convert(LocationProjectStatsDao dao) {
        Feature feature = new Feature();
        feature.setProperty(PROPERTY_LOC_NAME, dao.getName());
        feature.setProperty(PROPERTY_LOC_ID, dao.getId());

        feature.setProperty(PROPERTY_LOC_LEVEL,dao.getLevel());
        if (dao.getGeometry() != null) {
            feature.setGeometry(ConverterUtil.convert(dao.getGeometry()));
        } else {
            LOGGER.warn("Feature without geometry will be generated name is " + dao.getName());
        }
        feature.setProperty(PROPERTY_LOC_PHYSICAL_PROGRESS, dao.getPhysicalProgress());
        feature.setProperty(PROPERTY_LOC_PROJ_COUNT, dao.getProjectCount());
        return feature;
    }
}
