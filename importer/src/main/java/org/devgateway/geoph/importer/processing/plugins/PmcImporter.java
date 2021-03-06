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
@Service("pmcImporter")
public class PmcImporter extends GeophProjectsImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PmcImporter.class);
    private static final int MAX_LENGTH = 255;
    private static final String UNDEFINED = "undefined";
    private static final String LOCALLY_FUNDED = "locally-funded";
    private static final double UTILIZATION = 1D;
    public static final String DOUBLE_FIX = ".0";

    @Autowired
    private PmcColumns pmcColumns;

    @Override
    protected void addProject(Row row, final int rowNumber) {
        int currentRow = rowNumber - 1;
        Project p = new Project();
        try {
            String phId = getCorrectPhId(getStringValueFromCell(row.getCell(pmcColumns.getProjectId()), "project id", rowNumber, onProblem.NOTHING, false));
            if(StringUtils.isBlank(phId)) {
                addError(p.getPhId(), currentRow, "Project Id not found, the project won't be imported", true);
                return;
            }
            p.setPhId(phId);

            String title = getStringValueFromCell(row.getCell(pmcColumns.getProjectTitle()), "project title", rowNumber, onProblem.NOTHING, false);
            if(title.length()> MAX_LENGTH){
                p.setTitle(title.substring(0, MAX_LENGTH));
            } else {
                p.setTitle(title);
            }

            String fa = getStringValueFromCell(row.getCell(pmcColumns.getFundingInstitution()), "funding institution", rowNumber, onProblem.NOTHING, true);
            if(StringUtils.isBlank(fa) || importBaseData.getFundingAgencies().get(fa.trim()) == null){
                fa = LOCALLY_FUNDED;
                addWarning(p.getPhId(), currentRow, "Funding Agency not found, added as " + fa);
            }
            p.setFundingAgency(importBaseData.getFundingAgencies().get(fa.trim()));

            String[] ias = getStringArrayValueFromCell(row.getCell(pmcColumns.getImplementingAgency()), "implementing agency", rowNumber, onProblem.NOTHING);
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
                        addWarning(p.getPhId(), currentRow, "IA not found, added as undefined. IA: " + ia);
                        iaSet.add(new ProjectAgency(p, importBaseData.getImplementingAgencies().get(UNDEFINED), 0D));
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

            p.setGrantClassification(importBaseData.getClassifications().get(
                    getStringValueFromCell(row.getCell(pmcColumns.getClassification()), "classification", rowNumber, onProblem.NOTHING, true)
            ));

            String ea = getStringValueFromCell(row.getCell(pmcColumns.getExecutingAgency()), "executing agency", rowNumber, onProblem.NOTHING, true);
            if(StringUtils.isBlank(ea) || importBaseData.getExecutingAgencies().get(ea.trim()) == null){
                ea = UNDEFINED;
            }
            p.setExecutingAgency(importBaseData.getExecutingAgencies().get(ea.trim()));

            p.setOriginalCurrency(importBaseData.getCurrencies().get(
                    getStringValueFromCell(row.getCell(pmcColumns.getOriginalCurrency()), "original currency", rowNumber, onProblem.NOTHING, true)
            ));

            p.setTotalProjectAmountOriginal(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getPmcAmountInOriginalCurrency()), "pmc amount in original currency", rowNumber, onProblem.NOTHING)
            );

            p.setTotalProjectAmount(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getPmcAmount()), "pmc amount", rowNumber, onProblem.NOTHING, 0D)
            );

            PublicInvestment commitment = new PublicInvestment();
            commitment.setAmount(getDoubleValueFromCell(row.getCell(pmcColumns.getCumulativeAllotment()), "Cumulative allotment", rowNumber, onProblem.NOTHING, 0D));
            commitment.setTransactionTypeId(TransactionTypeEnum.COMMITMENTS.getId());
            commitment.setTransactionStatusId(TransactionStatusEnum.ACTUAL.getId());
            commitment.setDate(getImportDate());
            commitment.setProject(p);

            PublicInvestment disbursement = new PublicInvestment();
            disbursement.setAmount(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getCumulativeObligations()), "Cumulative Obligations", rowNumber, onProblem.NOTHING, 0D)
            );
            disbursement.setTransactionTypeId(typeId);
            disbursement.setTransactionStatusId(statusId);
            disbursement.setDate(getImportDate());
            
            disbursement.setProject(p);

            PublicInvestment expenditure = new PublicInvestment();
            expenditure.setAmount(getDoubleValueFromCell(row.getCell(pmcColumns.getTotalDisbursements()), "Total disbursement", rowNumber, onProblem.NOTHING, 0D));
            expenditure.setTransactionTypeId(TransactionTypeEnum.EXPENDITURES.getId());
            expenditure.setTransactionStatusId(TransactionStatusEnum.ACTUAL.getId());
            expenditure.setDate(getImportDate());
            expenditure.setProject(p);

            p.setTransactions(new HashSet<>(Arrays.asList(commitment, disbursement, expenditure)));

            p.setStartDate(
                    getDateValueFromCell(row.getCell(pmcColumns.getStartDate()), "start date", rowNumber, onProblem.NOTHING)
            );

            p.setEndDate(
                    getDateValueFromCell(row.getCell(pmcColumns.getOriginalClosingDate()), "original closing date", rowNumber, onProblem.NOTHING)
            );

            p.setRevisedClosingDate(
                    getDateValueFromCell(row.getCell(pmcColumns.getRevisedClosingDate()), "revised closing date", rowNumber, onProblem.NOTHING)
            );

            p.setRevisedClosingDate(
                    getDateValueFromCell(row.getCell(pmcColumns.getRevisedClosingDate()), "revised closing date", rowNumber, onProblem.NOTHING)
            );

            String sector = getStringValueFromCell(row.getCell(pmcColumns.getSectors()), "sectors", rowNumber, GeophProjectsImporter.onProblem.NOTHING, false);
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

            String[] agenda = getStringArrayValueFromCell(row.getCell(pmcColumns.getAgenda()), "socioeconomic agenda", rowNumber, onProblem.NOTHING);
            Set<Agenda> agendaSet = new HashSet<>();
            Arrays.asList(agenda).stream().forEach(x -> {
                String v = x.indexOf(DOUBLE_FIX) > 0 ? x.substring(0, x.indexOf(DOUBLE_FIX)).trim() : x.trim();
                if (importBaseData.getAgendas().get(v) != null) {
                    agendaSet.add(importBaseData.getAgendas().get(v));
                }
            });
            p.setAgendas(agendaSet);

            String[] pdp = getStringArrayValueFromCell(row.getCell(pmcColumns.getPdp()), "pdp", rowNumber, onProblem.NOTHING);
            Set<Pdp> pdpSet = new HashSet<>();
            Arrays.asList(pdp).stream().forEach(x -> {
                String v = x.indexOf(DOUBLE_FIX) > 0 ? x.substring(0, x.indexOf(DOUBLE_FIX)).trim() : x.trim();
                if (importBaseData.getPdps().get(v) != null) {
                    pdpSet.add(importBaseData.getPdps().get(v));
                }
            });
            p.setPdps(pdpSet);

            String[] sdg = getStringArrayValueFromCell(row.getCell(pmcColumns.getSdg()), "sdg", rowNumber, onProblem.NOTHING);
            Set<Sdg> sdgSet = new HashSet<>();
            Arrays.asList(sdg).stream().forEach(x -> {
                String v = x.indexOf(DOUBLE_FIX) > 0 ? x.substring(0, x.indexOf(DOUBLE_FIX)).trim() : x.trim();
                if (importBaseData.getSdgs().get(v) != null) {
                    sdgSet.add(importBaseData.getSdgs().get(v));
                }
            });
            p.setSdgs(sdgSet);

            String[] locations = getStringArrayValueFromCell(row.getCell(pmcColumns.getMunicipality()), "municipality", rowNumber, onProblem.NOTHING);
            if(locations.length==0){
                locations = getStringArrayValueFromCell(row.getCell(pmcColumns.getProvince()), "province", rowNumber, onProblem.NOTHING);
                if(locations.length==0){
                    locations = getStringArrayValueFromCell(row.getCell(pmcColumns.getRegion()), "region", rowNumber, onProblem.NOTHING);
                }
            }
            if(locations.length>0) {
                Set<ProjectLocation> locationSet = new HashSet<>();
                Set<Location> locationRegion = new HashSet<>();
                Set<Location> locationProvince = new HashSet<>();
                Set<Location> locationMunicipality = new HashSet<>();
                for (String loc : locations) {
                    String locOk = loc.trim().endsWith(DOUBLE_FIX)?loc.trim().substring(0,loc.length()-2):loc.trim();
                    if (StringUtils.isEmpty(locOk)) {
                        int a = 0;
                    }
                    Long locId = Long.parseLong(locOk);
                    Location l = importBaseData.getLocationsById().get(locId);
                    if(l!=null) {
                        if(l.getLevel()==LocationAdmLevelEnum.REGION.getLevel()){
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
                locationRegion.stream().forEach(l->locationSet.add(new ProjectLocation(p, l, UTILIZATION/locationRegion.size())));
                p.setLocations(locationSet);
            } else {
                addWarning(p.getPhId(), currentRow, "Location not found, Project was imported anyway");
            }

            Status status = importBaseData.getStatuses().get(getStringValueFromCell(row.getCell(pmcColumns.getStatus()), "status", rowNumber, onProblem.NOTHING, true));
            if(status==null){
                status = importBaseData.getStatuses().get(UNDEFINED);
            }
            p.setStatus(status);

            PhysicalStatus physicalStatus = importBaseData.getPhysicalStatuses().get(getStringValueFromCell(row.getCell(pmcColumns.getPhysicalStatus()), "physical status", rowNumber, onProblem.NOTHING, true));
            if(physicalStatus==null){
                physicalStatus = importBaseData.getPhysicalStatuses().get(UNDEFINED);
            }
            p.setPhysicalStatus(physicalStatus);

            p.setActualOwpa(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getActualOwpa()), "actual owpa", rowNumber, GeophProjectsImporter.onProblem.NOTHING)
            );

            p.setTargetOwpa(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getTargetOwpa()), "target owpa", rowNumber, GeophProjectsImporter.onProblem.NOTHING)
            );

            p.setPhysicalProgress(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getPhysicalProgress()), "physical performance", rowNumber, GeophProjectsImporter.onProblem.NOTHING)
            );

            p.setIssueType(
                    getStringValueFromCell(row.getCell(pmcColumns.getIssueType()), "issue type", rowNumber, GeophProjectsImporter.onProblem.NOTHING, true)
            );

            p.setIssueDetail(
                    getStringValueFromCell(row.getCell(pmcColumns.getIssueDetail()), "issue detail", rowNumber, GeophProjectsImporter.onProblem.NOTHING, true)
            );

            p.setCumulativeAllotment(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getCumulativeAllotment()), "cumulative allotment", rowNumber, GeophProjectsImporter.onProblem.NOTHING)
            );

            p.setCumulativeObligations(
                    getDoubleValueFromCell(row.getCell(pmcColumns.getCumulativeObligations()), "cumulative obligations", rowNumber, GeophProjectsImporter.onProblem.NOTHING)
            );

            String[] climates = getStringArrayValueFromCell(row.getCell(pmcColumns.getClimateChangeClassification()), "climate change", rowNumber, onProblem.NOTHING);
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

            String[] genders = getStringArrayValueFromCell(row.getCell(pmcColumns.getGenderClassification()), "gender classification", rowNumber, onProblem.NOTHING);
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
