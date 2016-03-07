package org.devgateway.geoph.rest;

import org.devgateway.geoph.model.*;
import org.devgateway.geoph.response.GenericResponse;
import org.devgateway.geoph.services.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * @author dbianco
 *         created on feb 29 2016.
 */

@RestController
@RequestMapping(value = "/filters", produces = "application/json", consumes = "application/json")
public class FilterController {

    private final FilterService service;

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterController.class);

    @Autowired
    public FilterController(FilterService service) {
        this.service = service;
    }

    @RequestMapping(value = "/impAgency", method = GET)
    //@Secured("ROLE_READ")
    public GenericResponse findAllImpAgencies() {
        LOGGER.debug("findAllImpAgencies");
        List<ImplementingAgency> impAgencies = service.findAllImpAgencies();
        GenericResponse resp = new GenericResponse(
                "Implementing Agencies",
                "ia",
                "IMPLEMENTING_AGENCY_SECTION",
                1,
                impAgencies,
                impAgencies.size()
        );

        return resp;
    }

    @RequestMapping(value = "/fundingAgency", method = GET)
    //@Secured("ROLE_READ")
    public GenericResponse findAllFundingAgencies() {
        LOGGER.debug("findAllFundingAgencies");
        List<FundingAgency> fundingAgencies = service.findAllFundingAgencies();
        GenericResponse resp = new GenericResponse(
                "Funding Agencies",
                "fa",
                "FUNDING_ORG_SECTION",
                1,
                fundingAgencies,
                fundingAgencies.size()
        );

        return resp;
    }

    @RequestMapping(value = "/flowType", method = GET)
    //@Secured("ROLE_READ")
    public GenericResponse findAllFlowTypes() {
        LOGGER.debug("findAllFlowTypes");
        List<FlowType> flowTypes = service.findAllFlowTypes();
        GenericResponse resp = new GenericResponse(
                "Flow Types",
                "ft",
                "FLOW_TYPE_SECTION",
                1,
                flowTypes,
                flowTypes.size()
        );

        return resp;
    }

    @RequestMapping(value = "/sectors", method = GET)
    //@Secured("ROLE_READ")
    public GenericResponse findAllSectors() {
        LOGGER.debug("findAllSectors");
        List<Sector> sectors = service.findAllSectors();
        GenericResponse resp = new GenericResponse(
                "Sectors",
                "st",
                "SECTORS_SECTION",
                1,
                sectors,
                sectors.size()
        );

        return resp;
    }
}