package org.devgateway.geoph.rest;

import org.devgateway.geoph.core.request.AppRequestParams;
import org.devgateway.geoph.core.request.Parameters;
import org.devgateway.geoph.core.services.GeoJsonService;
import org.devgateway.geoph.core.services.LayerService;
import org.devgateway.geoph.enums.GeometryDetailLevelEnum;
import org.devgateway.geoph.enums.LocationAdmLevelEnum;
import org.geojson.FeatureCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author dbianco
 *         created on mar 11 2016.
 */
@RestController
@RequestMapping(value = "/geodata")
public class GeoJsonController extends CrossOriginSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJsonController.class);

    private final GeoJsonService geoJsonService;

    private final LayerService layerService;

    @Autowired
    public GeoJsonController(GeoJsonService geoJsonService, LayerService layerService) {
        this.geoJsonService = geoJsonService;
        this.layerService = layerService;
    }

    @RequestMapping(value = "/{level}/projects", method = GET)
    public FeatureCollection getGeoJsonByLocationType(
            @PathVariable final String level,
            AppRequestParams filters) {
        LOGGER.debug("getGeoJsonByLocationType");
        Parameters params = filters.getParameters();
        params.setLocationLevel(level);
        return geoJsonService.getLocationsByParams(params);
    }

    @RequestMapping(value = "/stats/{level}/funding", method = GET)
    public FeatureCollection getGeoJsonStatistical(
            @PathVariable final String level, AppRequestParams filters) {
        LOGGER.debug("getGeoJsonForShapes");
        return geoJsonService.getShapesByLevelAndDetail(
                LocationAdmLevelEnum.valueOf(level.toUpperCase()),
                GeometryDetailLevelEnum.MEDIUM.getLevel(),
                filters.getParameters());
    }

    @RequestMapping(value = "/stats/{level}/funding/detail/{detail:.+}", method = GET)
    public FeatureCollection getGeoJsonStatisticalDetailed(
            @PathVariable final String level,
            @PathVariable final double detail,
            AppRequestParams filters) {
        LOGGER.debug("getGeoJsonForShapes with detail from param");
        return geoJsonService.getShapesByLevelAndDetail(
                LocationAdmLevelEnum.valueOf(level.toUpperCase()),
                detail,
                filters.getParameters());
    }

    @RequestMapping(value = "/indicators/id/{indicatorId}", method = GET)
    public FeatureCollection getIndicatorsData(@PathVariable final long indicatorId) {
        LOGGER.debug("getIndicatorsData for indicator id:" + indicatorId);
        return layerService.getIndicatorsData(indicatorId);
    }

    @RequestMapping(value = "/geophotos/id/{kmlId}", method = GET)
    public FeatureCollection getGeoPhotosData(@PathVariable final long kmlId) {
        LOGGER.debug("getGeoPhotosData for kml id:" + kmlId);
        return layerService.getGeoPhotoData(kmlId);
    }

}