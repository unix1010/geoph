package org.devgateway.geoph.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.devgateway.geoph.core.exceptions.BadRequestException;
import org.devgateway.geoph.core.services.AppMapService;
import org.devgateway.geoph.core.services.ApplicationService;
import org.devgateway.geoph.core.services.ScreenCaptureService;
import org.devgateway.geoph.core.util.MD5Generator;
import org.devgateway.geoph.dao.AppMapDao;
import org.devgateway.geoph.enums.AppMapTypeEnum;
import org.devgateway.geoph.model.AppMap;
import org.devgateway.geoph.persistence.spring.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author dbianco
 * created on abr 20 2016.
 */
@RestController
@RequestMapping(value = "/maps")
@CrossOrigin
@CacheConfig(keyGenerator = "genericFilterKeyGenerator", cacheNames = "mapControllerCache")
public class MapController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapController.class);
    private static final String NAME_STR = "name";
    private static final String DESCRIPTION_STR = "description";
    private static final String TYPE_STR = "type";
    private static final String DATA_TO_SAVE_STR = "data";
    private static final String BAD_REQUEST_NAME_INVALID = "The name used to save the map is not valid or it is already in use";
    private static final String SHARED_MAP_DESC = "Shared map";

    @Autowired
    private CacheConfiguration conf;

    @Autowired
    private AppMapService appMapService;

    @Autowired
    private ScreenCaptureService screenCaptureService;

    @Autowired
    private ApplicationService applicationService;

    @RequestMapping(method = GET)
    @Cacheable
    public Page<AppMapDao> findMaps(@PageableDefault(page = 0, size = 20, sort = "id") final Pageable pageable,
                                    @RequestParam(required = false) String type) {
        LOGGER.debug("findMaps");
        List<String> typeList = new ArrayList<>();
        if (StringUtils.isBlank(type) || type.equals("all") || type.equals(AppMapTypeEnum.DASHBOARD.getName())) {
            typeList.add(AppMapTypeEnum.DASHBOARD.getName());
            if (applicationService.isUserAuthenticated()) {
                typeList.add(AppMapTypeEnum.SAVE.getName());
            }
        } else {
            if (type.equals(AppMapTypeEnum.SAVE.getName()) && !applicationService.isUserAuthenticated()) {
                return null;
            }
            typeList.add(type);
        }

        return appMapService.findByType(typeList, pageable);
    }


    @RequestMapping(value = "/save", method = POST)
    @Secured("ROLE_ADMIN")
    public AppMap saveMap(@RequestBody Map<String, Object> mapVariables) throws IOException, SQLException {
        LOGGER.debug("saveMap");
        String mapName = (String) mapVariables.get(NAME_STR);
        String base64 = null;
        Long id = null;
        if (StringUtils.isNotBlank(mapVariables.get("id").toString())) {
            id = new Long((Integer) mapVariables.get("id"));
        }
        if (checkIfMapNameIsValid(mapName) || id != null) {
            String html = (String) mapVariables.get("html");
            Integer width = (Integer) mapVariables.get("width");
            Integer height = (Integer) mapVariables.get("height");
            Integer scaleWidth = (Integer) mapVariables.get("scaleWidth");
            Integer scaleHeight = (Integer) mapVariables.get("scaleHeight");
            if (mapVariables.get("id") != null && !mapVariables.get("id").equals("")) {
                id = new Long((Integer) mapVariables.get("id"));
            }

            if (html != null) {
                //get preview image
                BufferedImage image = screenCaptureService.captureImage(width, height, screenCaptureService.buildPage(width, height, html).toURI());

                if (scaleWidth != null) {
                    image = screenCaptureService.scaleWidth(image, scaleWidth);
                } else if (scaleHeight != null) {
                    image = screenCaptureService.scaleHeight(image, scaleHeight);
                }
                base64 = screenCaptureService.toBase64(image);

            }
            String mapDesc = (String) mapVariables.get(DESCRIPTION_STR);
            String mapJson = new ObjectMapper().writeValueAsString(mapVariables.get(DATA_TO_SAVE_STR));
            String mapType = (String) mapVariables.get(TYPE_STR);
            AppMap appMap = new AppMap(mapName, mapDesc, mapJson, UUID.randomUUID().toString(),
                    MD5Generator.getMD5(mapJson), mapType, base64);

            // clear cache
            conf.clearMapControllerCache();

            if (id == null) {
                return appMapService.save(appMap);
            } else {
                return appMapService.update(id, appMap);
            }
        } else {
            throw new BadRequestException(BAD_REQUEST_NAME_INVALID);
        }
    }

    @RequestMapping(value = "/share", method = POST)
    @Cacheable
    public AppMap shareMap(@RequestBody Map<String, Object> mapVariables) throws JsonProcessingException, SQLException {
        LOGGER.debug("shareMap");
        String mapJson = new ObjectMapper().writeValueAsString(mapVariables.get(DATA_TO_SAVE_STR));
        String md5 = MD5Generator.getMD5(mapJson);
        AppMap map = appMapService.findByMD5(md5);
        if (map == null) {
            String mapName = UUID.randomUUID().toString();
            String mapDesc = SHARED_MAP_DESC;
            map = appMapService.save(new AppMap(mapName, mapDesc, mapJson, mapName, md5,
                    AppMapTypeEnum.SHARE.getName(), null));
        }

        return map;
    }

    private boolean checkIfMapNameIsValid(String mapName) {
        boolean ret = false;
        if (StringUtils.isNotBlank(mapName)) {
            List<AppMap> maps = appMapService.findByName(mapName);
            if (maps == null || maps.size() == 0) {
                ret = true;
            }
        }
        return ret;
    }

    @RequestMapping(value = "/id/{id}", method = GET)
    @Cacheable
    public AppMap findMapById(@PathVariable final long id) {
        LOGGER.debug("findMapById");
        AppMap map = appMapService.findById(id);
        if (map != null && map.getType().equals(AppMapTypeEnum.SAVE.getName())
                && !applicationService.isUserAuthenticated()) {
            map = null;
        }
        return map;
    }

    @RequestMapping(value = "/id/{id}", method = DELETE)
    @Secured("ROLE_ADMIN")
    public void deleteById(@PathVariable final long id) {
        LOGGER.debug("deleteById: " + id);

        // clear cache
        conf.clearMapControllerCache();

        appMapService.delete(id);
    }

    @RequestMapping(value = "/key/{key}", method = DELETE)
    @Secured("ROLE_ADMIN")
    public void deleteByKey(@PathVariable final String key) {
        LOGGER.debug("deleteByKey: " + key);

        // clear cache
        conf.clearMapControllerCache();

        appMapService.deleteByKey(key);
    }

    @RequestMapping(value = "/key/{key}", method = GET)
    @Cacheable
    public AppMap findMapByKey(@PathVariable final String key) {
        LOGGER.debug("findMapByKey");
        return appMapService.findByKey(key);
    }


    @RequestMapping(value = "/search/{name}", method = GET)
    public List<AppMap> findMapByName(@PathVariable final String name) {
        LOGGER.debug("findMapByKey");
        return appMapService.findByNameOrDescription(name);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAppException(Exception ex) {
        LOGGER.error("Can't complete this request", ex);
        return ex.getMessage();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleBadRequestException(BadRequestException exception) {
        Map<String, Object> result = new HashMap<>();
        result.put("error", "Bad Request");
        result.put("message", exception.getMessage());
        result.put("status", 400);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}

