package org.devgateway.geoph.importer.processing.plugins;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.devgateway.geoph.enums.LocationAdmLevelEnum;
import org.devgateway.geoph.enums.TransactionStatusEnum;
import org.devgateway.geoph.enums.TransactionTypeEnum;
import org.devgateway.geoph.importer.processing.GeophProjectsImporter;
import org.devgateway.geoph.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author dbianco
 *         created on jul 04 2016.
 */
@Service("grantImporter")
public class GrantImporter extends GeophProjectsImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrantImporter.class);

    private static final int MAX_LENGTH = 255;
    private static final String UNDEFINED = "undefined";
    private static final double UTILIZATION = 1D;

    @Autowired
    private GrantColumns grantColumns;

    @Override
    protected void addProject(Row row, final int rowNumber) {
        int currentRow = rowNumber - 1;
        Project p = new Project();
        try {
            String phId = getCorrectPhId(getStringValueFromCell(row.getCell(grantColumns.getProjectId()), "project id", rowNumber, onProblem.NOTHING, false));
            if(StringUtils.isBlank(phId)) {
                addError(p.getPhId(), currentRow, "Project Id not found, the project won't be imported", true);
                return;
            }
            p.setPhId(phId);
            String title = getStringValueFromCell(row.getCell(grantColumns.getProjectTitle()), "project title", rowNumber, onProblem.NOTHING, false);
            if(title.length()> MAX_LENGTH){
                p.setTitle(title.substring(0, MAX_LENGTH));
            } else {
                p.setTitle(title);
            }

            String fa = getStringValueFromCell(row.getCell(grantColumns.getFundingInstitution()), "funding institution", rowNumber, onProblem.NOTHING, true);
            if(StringUtils.isBlank(fa) || importBaseData.getFundingAgencies().get(fa.trim()) == null){
                fa = UNDEFINED;
                addWarning(p.getPhId(), currentRow, "Funding Agency not found, added as " + fa);
            }
            p.setFundingAgency(importBaseData.getFundingAgencies().get(fa.trim()));

            String[] ias = getStringArrayValueFromCell(row.getCell(grantColumns.getImplementingAgency()), "implementing agency", rowNumber, onProblem.NOTHING);
            Set<ProjectAgency> iaSet = new HashSet<>();
            boolean isFirstPA = true;
            for (String ia : ias) {
                if (importBaseData.getImplementingAgencies().get(ia.toLowerCase().trim()) != null) {
                    ProjectAgency pa;
                    if(isFirstPA) {
                        pa = new ProjectAgency(p, importBaseData.getImplementingAgencies().get(ia.toLowerCase().trim()), UTILIZATION);
                        isFirstPA = false;
                    } else {
                        pa = new ProjectAgency(p, importBaseData.getImplementingAgencies().get(ia.toLowerCase().trim()), 0D);
                    }
                    iaSet.add(pa);
                } else {
                    if(isFirstPA){
                        addError(p.getPhId(), currentRow, "IA not found at first value, the project won't be imported. IA: " + ia, true);
                        return;
                    } else {
                        addWarning(p.getPhId(), currentRow, "IA not found, added as undefined. IA: " + ia);
                        iaSet.add(new ProjectAgency(p, importBaseData.getImplementingAgencies().get(UNDEFINED), 0D));
                    }
                }
            }
            if(iaSet.size()>0){
                p.setImplementingAgencies(iaSet);
            } else if(ias.length==0) {
                ProjectAgency pa = new ProjectAgency(p, importBaseData.getImplementingAgencies().get(UNDEFINED), UTILIZATION);
                p.setImplementingAgencies(new HashSet(Arrays.asList(pa)));
                addWarning(p.getPhId(), currentRow, "IA not found, added as undefined");
            }

            Classification cl = importBaseData.getClassifications().get(getStringValueFromCell(row.getCell(grantColumns.getClassification()), "classification", rowNumber, onProblem.NOTHING, true));
            if(cl == null){
                cl = importBaseData.getClassifications().get("other programs/projects");
                addWarning(p.getPhId(), currentRow, "Grant Classification not fount, added as Other Programs");
            }
            p.setGrantClassification(cl);

            String ea = getStringValueFromCell(row.getCell(grantColumns.getExecutingAgency()), "executing agency", rowNumber, onProblem.NOTHING, true);
            if(StringUtils.isBlank(ea) || importBaseData.getExecutingAgencies().get(ea.trim()) == null){
                ea = UNDEFINED;
            }
            p.setExecutingAgency(importBaseData.getExecutingAgencies().get(ea.trim()));

            p.setOriginalCurrency(importBaseData.getCurrencies().get(
                    getStringValueFromCell(row.getCell(grantColumns.getOriginalCurrency()), "original currency", rowNumber, onProblem.NOTHING, true)
            ));

            p.setTotalProjectAmountOriginal(
                    getDoubleValueFromCell(row.getCell(grantColumns.getGrantAmountInOriginalCurrency()), "project amount in original currency", rowNumber, onProblem.NOTHING)
            );

            p.setTotalProjectAmount(
                    getDoubleValueFromCell(row.getCell(grantColumns.getGrantAmount()), "project amount", rowNumber, onProblem.NOTHING, 0D)
            );

            Grant commitment = new Grant();
            commitment.setAmount(getDoubleValueFromCell(row.getCell(grantColumns.getGrantAmount()), "project amount", rowNumber, onProblem.NOTHING, 0D));
            commitment.setTransactionTypeId(TransactionTypeEnum.COMMITMENTS.getId());
            commitment.setTransactionStatusId(TransactionStatusEnum.ACTUAL.getId());
            commitment.setDate(getImportDate());
            GrantSubType grantSubType = importBaseData.getGrantSubTypes().get(
                    getStringValueFromCell(row.getCell(grantColumns.getSubType()), "grant subType", rowNumber, onProblem.NOTHING, true)
            );
            if(grantSubType!=null){
                commitment.setGrantSubTypeId(grantSubType.getId());
            } else {
                commitment.setGrantSubTypeId(importBaseData.getGrantSubTypes().get(UNDEFINED).getId());
            }
            commitment.setProject(p);

            Grant grant = new Grant();
            grant.setAmount(
                    getDoubleValueFromCell(row.getCell(grantColumns.getGrantUtilization()), "grant utilization", rowNumber, onProblem.NOTHING, 0D)
            );
            grant.setTransactionTypeId(typeId);
            grant.setTransactionStatusId(statusId);
            grant.setDate(getImportDate());

            if(grantSubType!=null){
                grant.setGrantSubTypeId(grantSubType.getId());
            }
            grant.setProject(p);
            p.setTransactions(new HashSet<>(Arrays.asList(commitment, grant)));

            p.setStartDate(
                    getDateValueFromCell(row.getCell(grantColumns.getStartDate()), "start date", rowNumber, onProblem.NOTHING)
            );

            p.setEndDate(
                    getDateValueFromCell(row.getCell(grantColumns.getOriginalClosingDate()), "original closing date", rowNumber, onProblem.NOTHING)
            );

            p.setRevisedClosingDate(
                    getDateValueFromCell(row.getCell(grantColumns.getRevisedClosingDate()), "revised closing date", rowNumber, onProblem.NOTHING)
            );

            p.setRevisedClosingDate(
                    getDateValueFromCell(row.getCell(grantColumns.getRevisedClosingDate()), "revised closing date", rowNumber, onProblem.NOTHING)
            );

            String sector = getStringValueFromCell(row.getCell(grantColumns.getSectors()), "sectors", rowNumber, GeophProjectsImporter.onProblem.NOTHING, false);
            Set<ProjectSector> sectorSet = new HashSet<>();
            Sector sectorObj;
            if (sector!=null && importBaseData.getSectors().get(sector.toLowerCase().trim()) != null) {
                sectorObj = importBaseData.getSectors().get(sector.toLowerCase().trim());
            } else {
                sectorObj = importBaseData.getSectors().get(UNDEFINED);
            }
            ProjectSector ps = new ProjectSector(p, sectorObj, UTILIZATION);
            sectorSet.add(ps);
            p.setSectors(sectorSet);

            String[] locations = getStringArrayValueFromCell(row.getCell(grantColumns.getMunicipality()), "municipality", rowNumber, onProblem.NOTHING);
            if(locations.length==0){
                locations = getStringArrayValueFromCell(row.getCell(grantColumns.getProvince()), "province", rowNumber, onProblem.NOTHING);
                if(locations.length==0){
                    locations = getStringArrayValueFromCell(row.getCell(grantColumns.getRegion()), "region", rowNumber, onProblem.NOTHING);
                }
            }
            if(locations.length>0) {
                Set<ProjectLocation> locationSet = new HashSet<>();
                Set<Location> locationRegion = new HashSet<>();
                Set<Location> locationProvince = new HashSet<>();
                Set<Location> locationMunicipality = new HashSet<>();
                for (String loc : locations) {
                    String locOk = loc.trim().endsWith(".0")?loc.trim().substring(0,loc.length()-2):loc.trim();
                    Location l = importBaseData.getLocations().get(locOk);
                    if(l!=null) {
                        if(l.getLevel()== LocationAdmLevelEnum.REGION.getLevel()){
                            locationRegion.add(l);
                        } else if(l.getLevel()==LocationAdmLevelEnum.PROVINCE.getLevel()){
                            locationProvince.add(l);
                            if(l.getRegion()!=null) {
                                locationRegion.add(l.getRegion());
                            }
                        } else if(l.getLevel()==LocationAdmLevelEnum.MUNICIPALITY.getLevel()){
                            locationMunicipality.add(l);
                            if(l.getProvince()!=null) {
                                locationProvince.add(l.getProvince());
                            }
                            if(l.getRegion()!=null) {
                                locationRegion.add(l.getRegion());
                            }
                        }
                    }
                }
                locationMunicipality.stream().forEach(l->locationSet.add(new ProjectLocation(p, l, 0D)));
                locationProvince.stream().forEach(l->locationSet.add(new ProjectLocation(p, l, 0D)));
                locationRegion.stream().forEach(l->locationSet.add(new ProjectLocation(p, l, UTILIZATION /locationRegion.size())));
                p.setLocations(locationSet);
            } else {
                addWarning(p.getPhId(), currentRow, "Location not found, Project was imported anyway");
            }

            Status status = importBaseData.getStatuses().get(getStringValueFromCell(row.getCell(grantColumns.getStatus()), "status", rowNumber, onProblem.NOTHING, true));
            if(status==null){
                status = importBaseData.getStatuses().get(UNDEFINED);
            }
            p.setStatus(status);

            PhysicalStatus physicalStatus = importBaseData.getPhysicalStatuses().get(getStringValueFromCell(row.getCell(grantColumns.getPhysicalStatus()), "physical status", rowNumber, onProblem.NOTHING, true));
            if(physicalStatus==null){
                physicalStatus = importBaseData.getPhysicalStatuses().get(UNDEFINED);
            }
            p.setPhysicalStatus(physicalStatus);

            p.setPeriodPerformanceStart(
                    getDateValueFromCell(row.getCell(grantColumns.getPeriodPerformanceStart()), "period performance start", rowNumber, onProblem.NOTHING)
            );

            p.setPeriodPerformanceEnd(
                    getDateValueFromCell(row.getCell(grantColumns.getPeriodPerformanceEnd()), "period performance end", rowNumber, onProblem.NOTHING)
            );



            String[] climates = getStringArrayValueFromCell(row.getCell(grantColumns.getClimateChangeClassification()), "climate change", rowNumber, onProblem.NOTHING);
            Set<ProjectClimateChange> climatesSet = new HashSet<>();
            boolean isFirstCC = true;
            for (String cc : climates) {
                if (importBaseData.getClimateChanges().get(cc.toLowerCase().trim()) != null) {
                    ProjectClimateChange pa;
                    if(isFirstCC) {
                        pa = new ProjectClimateChange(p, importBaseData.getClimateChanges().get(cc.toLowerCase().trim()), UTILIZATION);
                        isFirstCC = false;
                    } else {
                        pa = new ProjectClimateChange(p, importBaseData.getClimateChanges().get(cc.toLowerCase().trim()), 0D);
                    }
                    climatesSet.add(pa);
                }
            }
            if(climatesSet.size()==0){
                climatesSet.add(new ProjectClimateChange(p, importBaseData.getClimateChanges().get(UNDEFINED), UTILIZATION));
            }
            p.setClimateChange(climatesSet);

            String[] genders = getStringArrayValueFromCell(row.getCell(grantColumns.getGenderClassification()), "gender classification", rowNumber, onProblem.NOTHING);
            Set<ProjectGenderResponsiveness> genderSet = new HashSet<>();
            boolean isFirstGR = true;
            for (String gender : genders) {
                if (importBaseData.getGenderResponsiveness().get(gender.toLowerCase().trim()) != null) {
                    ProjectGenderResponsiveness pa;
                    if(isFirstGR) {
                        pa = new ProjectGenderResponsiveness(p, importBaseData.getGenderResponsiveness().get(gender.toLowerCase().trim()), UTILIZATION);
                        isFirstGR = false;
                    } else {
                        pa = new ProjectGenderResponsiveness(p, importBaseData.getGenderResponsiveness().get(gender.toLowerCase().trim()), 0D);
                    }
                    genderSet.add(pa);
                }
            }
            if(genderSet.size()==0){
                genderSet.add(new ProjectGenderResponsiveness(p, importBaseData.getGenderResponsiveness().get(UNDEFINED), UTILIZATION));
            }
            p.setGenderResponsiveness(genderSet);

            importBaseData.getProjectService().save(p);
            importStats.addSuccessProjectAndTransactions(p.getTransactions().size());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
