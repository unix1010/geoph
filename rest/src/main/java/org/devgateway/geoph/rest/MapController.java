package org.devgateway.geoph.rest;


import org.devgateway.geoph.core.services.AppMapService;
import org.devgateway.geoph.core.services.ScreenCaptureService;
import org.devgateway.geoph.model.AppMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author dbianco
 *         created on abr 20 2016.
 */
@RestController
@RequestMapping(value = "/maps")
public class MapController {


    private static final Logger LOGGER = LoggerFactory.getLogger(MapController.class);

    private final AppMapService appMapService;


    private final ScreenCaptureService screenCaptureService;

    @Autowired
    public MapController(AppMapService appMapService, ScreenCaptureService screenCaptureService) {
        this.appMapService = appMapService;
        this.screenCaptureService = screenCaptureService;
    }

    @RequestMapping(method = GET)
    public Page<AppMap> findMaps(@PageableDefault(page = 0, size = 20, sort = "id") final Pageable pageable) {
        LOGGER.debug("findMaps");
        return appMapService.findAll(pageable);
    }

    @RequestMapping(value = "/save", method = POST)
    public AppMap saveMap(@RequestParam(value = "name", required = true) String name,
                          @RequestParam(value = "description", required = true) String description,
                          @RequestBody Object mapVariables) {
        LOGGER.debug("saveMap");
        AppMap appMap = new AppMap(name, description, mapVariables.toString());
        return appMapService.save(appMap);
    }

    @RequestMapping(value = "/id/{id}", method = GET)
    public AppMap findMapById(@PathVariable final long id) {
        LOGGER.debug("findMapById");
        return appMapService.findById(id);
    }

    @RequestMapping(value = "/key/{key}", method = GET)
    public AppMap findMapByKey(@PathVariable final String key) {
        LOGGER.debug("findMapByKey");
        return appMapService.findByKey(key);
    }


    @RequestMapping(value = "/search/{name}", method = GET)
    public List<AppMap> findMapByName(@PathVariable final String name) {
        LOGGER.debug("findMapByKey");
        return appMapService.findByNameOrDescription(name);
    }

    @RequestMapping(value = "/print", method = GET)
<<<<<<< HEAD
    public String printPage(@RequestParam(value = "url", required = true) String url){
        String filename = null;
        try {
            if(StringUtils.isNotBlank(PropsHelper.getScreenFirefoxExe())) {
                File pathToBinary = new File(PropsHelper.getScreenFirefoxExe());
                FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
                FirefoxProfile firefoxProfile = new FirefoxProfile();
                WebDriver driver = new FirefoxDriver(ffBinary, firefoxProfile);
                driver.get(url);
                Thread.sleep(PropsHelper.getScreenCaptureTimeToWait());
                filename = getRandomKey() + ".png";
                File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.copyFile(scrFile, new File(PropsHelper.getScreenCaptureDir() + filename));
                driver.close();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return filename;
    }

    @RequestMapping(value = "/export/fileType/{fileType}/language/{language}", method = GET)
    public String exportData(
            @PathVariable final String fileType,
            @PathVariable final String language,
            @RequestParam(value = FILTER_START_DATE_MAX, required = false) String startDateMax,
            @RequestParam(value = FILTER_START_DATE_MIN, required = false) String startDateMin,
            @RequestParam(value = FILTER_END_DATE_MAX, required = false) String endDateMax,
            @RequestParam(value = FILTER_END_DATE_MIN, required = false) String endDateMin,
            @RequestParam(value = FILTER_PERFORMANCE_START_MAX, required = false) String performanceStartMax,
            @RequestParam(value = FILTER_PERFORMANCE_START_MIN, required = false) String performanceStartMin,
            @RequestParam(value = FILTER_PERFORMANCE_END_MAX, required = false) String performanceEndMax,
            @RequestParam(value = FILTER_PERFORMANCE_END_MIN, required = false) String performanceEndMin,
            @RequestParam(value = FILTER_SECTOR, required = false) String sectors,
            @RequestParam(value = FILTER_STATUS, required = false) String statuses,
            @RequestParam(value = FILTER_LOCATION, required = false) String locations,
            @RequestParam(value = FILTER_PROJECT, required = false) String projects,
            @RequestParam(value = FILTER_IMPLEMENTING_AGENCY, required = false) String impAgencies,
            @RequestParam(value = FILTER_FUNDING_AGENCY, required = false) String fundingAgencies,
            @RequestParam(value = FILTER_FLOW_TYPE, required = false) String flowTypes,
            @RequestParam(value = FILTER_PROJECT_TITLE, required = false) String projectTitle,
            @RequestParam(value = FILTER_PHYSICAL_STATUS, required = false) String physicalStatuses,
            @RequestParam(value = FILTER_CLIMATE_CHANGE, required = false) String climateChange,
            @RequestParam(value = FILTER_GENDER_RESPONSIVENESS, required = false) String genderResponsiveness,
            @RequestParam(value = FILTER_FINANCIAL_AMOUNT_MAX, required = false) Double financialAmountMax,
            @RequestParam(value = FILTER_FINANCIAL_AMOUNT_MIN, required = false) Double financialAmountMin){
        LOGGER.debug("exportData");
        Parameters params = new Parameters(startDateMax, startDateMin, endDateMax, endDateMin, performanceStartMax,
                performanceStartMin, performanceEndMax, performanceEndMin, sectors, statuses, locations,
                projects, impAgencies, fundingAgencies, flowTypes,
                projectTitle, physicalStatuses, climateChange, genderResponsiveness,
                financialAmountMin, financialAmountMax, null);
        List<Location> locationList = geoJsonService.getLocationsForExport(params);

        String filename = null;

        if(fileType.toLowerCase().trim().equals("xls")){
            filename = getExcelFile(language, locationList);
        } else {
            filename = getCsvFile(language, locationList);
        }
        return filename;
    }

    private String getCsvFile(String language, List<Location> locationList) {
        String filename = "NEDA_data_" + getRandomKey() + ".csv";
        try {
            FileWriter writer = new FileWriter(PropsHelper.getExportDir() + filename);
            String[] titles = null;
            if(language.toLowerCase().trim().equals("ph")){
                //TODO
                titles = EXPORT_ENGLISH_TITLE_ARRAY;
            } else {
                titles = EXPORT_ENGLISH_TITLE_ARRAY;
            }
            for(int i=0; i<titles.length; i++) {
                writer.append(titles[i]);
                if(i!=titles.length-1){
                    writer.append(',');
                }
            }
            writer.append(System.getProperty("line.separator"));
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            for(Location l : locationList) {
                for (Project p : l.getProjects()) {
                    writer.append(""+l.getId());
                    writer.append(COMMA +l.getCode());
                    writer.append(COMMA +l.getLevel());
                    writer.append(COMMA +l.getName().replace(',', '/'));
                    writer.append(COMMA +l.getLatitude());
                    writer.append(COMMA +l.getLongitude());
                    writer.append(COMMA +l.getRegionId());
                    writer.append(l.getProvinceId() != null ? COMMA +l.getProvinceId().toString() : COMMA);
                    writer.append(COMMA +p.getPhId());
                    writer.append(COMMA +p.getTitle().replace(',', '/'));
                    writer.append(COMMA);
                    StringBuilder iaSb = new StringBuilder();
                    for(Agency ia : p.getImplementingAgencies()){
                        iaSb.append(ia.getCode() + "/");
                    }
                    if(iaSb.length()>2){
                        writer.append(iaSb.toString().substring(0, iaSb.length()-2));
                    }
                    writer.append(p.getExecutingAgency()!=null? COMMA +p.getExecutingAgency().getName(): COMMA);
                    writer.append(p.getFundingAgency()!=null? COMMA +p.getFundingAgency().getName(): COMMA);
                    writer.append(p.getOriginalCurrency()!=null? COMMA +p.getOriginalCurrency().getName(): COMMA);
                    writer.append(COMMA +p.getTotalProjectAmount());

                    writer.append(COMMA + formatter.format(p.getStartDate()));
                    writer.append(COMMA + formatter.format(p.getEndDate()));
                    writer.append(COMMA + formatter.format(p.getRevisedClosingDate()));

                    writer.append(COMMA);
                    StringBuilder sectorSb = new StringBuilder();
                    for(Sector s : p.getSectors()){
                        sectorSb.append(s.getCode() + "/");
                    }
                    if(sectorSb.length()>2){
                        writer.append(sectorSb.toString().substring(0, sectorSb.length()-2));
                    }

                    writer.append(COMMA + formatter.format(p.getPeriodPerformanceStart()));
                    writer.append(COMMA + formatter.format(p.getPeriodPerformanceEnd()));

                    writer.append(p.getStatus()!=null?COMMA+p.getStatus().getName():COMMA);
                    writer.append(p.getPhysicalStatus()!=null?COMMA+p.getPhysicalStatus().getName():COMMA);

                    writer.append(COMMA); //TODO Physical performance
                    writer.append(COMMA);

                    writer.append(p.getGrantClassification()!=null?COMMA+p.getGrantClassification().getName():COMMA);
                    long disbursements = 0;
                    long commitments = 0;
                    for(Transaction t : p.getTransactions()){
                        if(t.getTransactionType().getId() == TransactionTypeEnum.DISBURSEMENT.getId()){
                            disbursements += t.getAmount();
                        }
                        if(t.getTransactionType().getId() == TransactionTypeEnum.COMMITMENT.getId()){
                            commitments += t.getAmount();
                        }
                    }
                    writer.append(COMMA+disbursements);
                    writer.append(COMMA+commitments);
                    writer.append(System.getProperty("line.separator"));
                }
            }
            writer.flush();
            writer.close();
        } catch(IOException e) {
            LOGGER.error(e.getMessage());
        }
        return filename;
    }

    private String getExcelFile(String language, List<Location> locationList) {
        String filename = "";
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Geoph export");
        Row row = sheet.createRow((short)0);
        String[] titles = null;
        if(language.toLowerCase().trim().equals("ph")){
            //TODO
            titles = EXPORT_ENGLISH_TITLE_ARRAY;
        } else {
            titles = EXPORT_ENGLISH_TITLE_ARRAY;
        }
        CellStyle titleStyle = getCellStyle(wb);
        for(int i=0; i<titles.length; i++){
            Cell cell = row.createCell((short) i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(titleStyle);
        }
        short rowNumber = 0;
        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm"));

        for(Location l : locationList){
            for(Project p : l.getProjects()){
                rowNumber ++;
                Row dataRow = sheet.createRow(rowNumber);
                dataRow.createCell(0).setCellValue(l.getId());
                dataRow.createCell(1).setCellValue(l.getCode());
                dataRow.createCell(2).setCellValue(l.getLevel());
                dataRow.createCell(3).setCellValue(l.getName());
                dataRow.createCell(4).setCellValue(l.getLatitude());
                dataRow.createCell(5).setCellValue(l.getLongitude());
                dataRow.createCell(6).setCellValue(l.getRegionId());
                dataRow.createCell(7).setCellValue(l.getProvinceId() != null ? l.getProvinceId().toString() : "");
                dataRow.createCell(8).setCellValue(p.getPhId());
                dataRow.createCell(9).setCellValue(p.getTitle());
                StringBuilder iaSb = new StringBuilder();
                for(Agency ia : p.getImplementingAgencies()){
                    iaSb.append(ia.getCode() + ", ");
                }
                if(iaSb.length()>3){
                    dataRow.createCell(10).setCellValue(iaSb.toString().substring(0, iaSb.length()-3));
                }
                dataRow.createCell(11).setCellValue(p.getExecutingAgency()!=null?p.getExecutingAgency().getName():"");
                dataRow.createCell(12).setCellValue(p.getFundingAgency()!=null?p.getFundingAgency().getName():"");
                dataRow.createCell(13).setCellValue(p.getOriginalCurrency()!=null?p.getOriginalCurrency().getName():"");
                dataRow.createCell(14).setCellValue(p.getTotalProjectAmount());

                Cell cellStartDate = dataRow.createCell(15);
                cellStartDate.setCellValue(p.getStartDate());
                cellStartDate.setCellStyle(dataStyle);

                Cell cellEndDate = dataRow.createCell(16);
                cellEndDate.setCellValue(p.getEndDate());
                cellEndDate.setCellStyle(dataStyle);

                Cell cellRevisedClosingDate = dataRow.createCell(17);
                cellRevisedClosingDate.setCellValue(p.getRevisedClosingDate());
                cellRevisedClosingDate.setCellStyle(dataStyle);

                StringBuilder sectorSb = new StringBuilder();
                for(Sector s : p.getSectors()){
                    sectorSb.append(s.getCode() + ", ");
                }
                if(sectorSb.length()>3){
                    dataRow.createCell(18).setCellValue(sectorSb.toString().substring(0, sectorSb.length()-3));
                }

                Cell cellPeriodPerformanceStart = dataRow.createCell(19);
                cellPeriodPerformanceStart.setCellValue(p.getPeriodPerformanceStart());
                cellPeriodPerformanceStart.setCellStyle(dataStyle);

                Cell cellPeriodPerformanceEnd = dataRow.createCell(20);
                cellPeriodPerformanceEnd.setCellValue(p.getPeriodPerformanceEnd());
                cellPeriodPerformanceEnd.setCellStyle(dataStyle);

                dataRow.createCell(21).setCellValue(p.getStatus()!=null?p.getStatus().getName():"");
                dataRow.createCell(22).setCellValue(p.getPhysicalStatus()!=null?p.getPhysicalStatus().getName():"");
                dataRow.createCell(23).setCellValue(""); //TODO Physical performance
                dataRow.createCell(24).setCellValue("");
                dataRow.createCell(25).setCellValue(p.getGrantClassification()!=null?p.getGrantClassification().getName():"");
                long disbursements = 0;
                long commitments = 0;
                for(Transaction t : p.getTransactions()){
                    if(t.getTransactionType().getId() == TransactionTypeEnum.DISBURSEMENT.getId()){
                        disbursements += t.getAmount();
                    }
                    if(t.getTransactionType().getId() == TransactionTypeEnum.COMMITMENT.getId()){
                        commitments += t.getAmount();
                    }
                }
                dataRow.createCell(26).setCellValue(disbursements);
                dataRow.createCell(27).setCellValue(commitments);
            }
        }

        try {
            filename = "NEDA_data_" + getRandomKey() + ".xls";
            FileOutputStream fileOut = new FileOutputStream(PropsHelper.getExportDir()+filename);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return filename;
    }

    private CellStyle getCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style.setFont(font);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setWrapText(true);
        return style;
=======
    public String printPage(@RequestParam(value = "url", required = true) String url) throws Exception {
        return screenCaptureService.captureUrlToImage(url);
>>>>>>> master
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAppException(Exception ex) {
        LOGGER.error("Can't complete this request", ex);
        return ex.getMessage();
    }

}
