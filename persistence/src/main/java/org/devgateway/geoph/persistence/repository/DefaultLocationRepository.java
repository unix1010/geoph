package org.devgateway.geoph.persistence.repository;

import com.vividsolutions.jts.geom.Geometry;
import org.devgateway.geoph.core.repositories.LocationRepository;
import org.devgateway.geoph.core.request.Parameters;
import org.devgateway.geoph.dao.GeometryDao;
import org.devgateway.geoph.dao.LocationProjectStatsDao;
import org.devgateway.geoph.dao.LocationResultsDao;
import org.devgateway.geoph.dao.ProjectLocationDao;
import org.devgateway.geoph.model.*;
import org.devgateway.geoph.persistence.util.FilterHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.devgateway.geoph.core.constants.Constants.*;

/**
 * @author dbianco
 *         created on mar 14 2016.
 */
@Service
public class DefaultLocationRepository implements LocationRepository {

    @Autowired
    EntityManager em;

    @Override
    public List<Location> findAll() {
        return em.createNamedQuery("findAllLocations", Location.class).getResultList();
    }

    @Override
    @Cacheable("locationsById")
    public Location findById(long id) {
        return em.createNamedQuery("findLocationsById", Location.class)
                .setParameter(PROPERTY_LOC_ID, id)
                .getSingleResult();
    }

    @Override
    @Cacheable("locationsByCode")
    public Location findByCode(String code) {
        return em.createNamedQuery("findLocationsByCode", Location.class)
                .setParameter(PROPERTY_LOC_CODE, code)
                .getSingleResult();
    }

    @Override
    @Cacheable("locationsByLevel")
    public List<Location> findLocationsByLevel(int level) {
        return em.createNamedQuery("findLocationsByLevel", Location.class)
                .setParameter(PROPERTY_LOC_LEVEL, level)
                .getResultList();
    }

    @Override
    @Cacheable("locationsByLevelUacsNotNull")
    public List<Location> findLocationsByLevelUacsNotNull(int level) {
        return em.createNamedQuery("findLocationsByLevelUacsNotNull", Location.class)
                .setParameter(PROPERTY_LOC_LEVEL, level)
                .getResultList();
    }

    @Override
    @Cacheable("findLocationsByParentId")
    public List<Location> findLocationsByParentId(long parentId) {
        return em.createNativeQuery("Select l.* from location l " +
                "inner join location_items li on l.id=li.items_id " +
                "where li.location_id = :parentId", Location.class)
                .setParameter("parentId", parentId)
                .getResultList();
    }

    @Override
    @Cacheable("countLocationProjectsByParams")
    public List<LocationResultsDao> countLocationProjectsByParams(Parameters params) {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<LocationResultsDao> criteriaQuery = criteriaBuilder.createQuery(LocationResultsDao.class);

        Root<Location> locationRoot = criteriaQuery.from(Location.class);
        List<Selection<?>> multiSelect = new ArrayList<>();
        multiSelect.add(locationRoot);

        List<Predicate> predicates = new ArrayList<>();
        List<Expression<?>> groupByList = new ArrayList<>();
        groupByList.add(locationRoot);

        Join<Location, ProjectLocation> projectLocationJoin = locationRoot.join(Location_.projects, JoinType.LEFT);
        Join<ProjectLocation, ProjectLocationId> idJoin = projectLocationJoin.join(ProjectLocation_.pk, JoinType.LEFT);
        Join<ProjectLocationId, Project> projectJoin = idJoin.join(ProjectLocationId_.project, JoinType.LEFT);
        multiSelect.add(criteriaBuilder.countDistinct(projectJoin).alias("projectCount"));

        FilterHelper.filterLocationQuery(params, criteriaBuilder, locationRoot, predicates, projectJoin, null, projectLocationJoin, null);

        Predicate other = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        criteriaQuery.where(other);

        criteriaQuery.groupBy(groupByList);
        TypedQuery<LocationResultsDao> query = em.createQuery(criteriaQuery.multiselect(multiSelect));

        return query.getResultList();
    }

    @Override
    @Cacheable("locationsByParams")
    public List<ProjectLocationDao> findProjectLocationsByParams(Parameters params) {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ProjectLocationDao> criteriaQuery = criteriaBuilder.createQuery(ProjectLocationDao.class);

        Root<Location> locationRoot = criteriaQuery.from(Location.class);
        Join<Location, ProjectLocation> projectLocationJoin = locationRoot.join(Location_.projects, JoinType.INNER);
        Join<ProjectLocation, ProjectLocationId> idJoin = projectLocationJoin.join(ProjectLocation_.pk, JoinType.INNER);
        Join<ProjectLocationId, Project> projectJoin = idJoin.join(ProjectLocationId_.project, JoinType.INNER);

        List<Selection<?>> multiSelect = new ArrayList<>();
        multiSelect.add(locationRoot);
        multiSelect.add(projectJoin);

        List<Expression<?>> groupByList = new ArrayList<>();
        groupByList.add(locationRoot);
        groupByList.add(projectJoin);

        List<Predicate> predicates = new ArrayList<>();
        FilterHelper.filterLocationQuery(params, criteriaBuilder, locationRoot, predicates, projectJoin, null, projectLocationJoin, null);

        Predicate predicate = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        criteriaQuery.where(predicate);

        criteriaQuery.groupBy(groupByList);
        TypedQuery<ProjectLocationDao> query = em.createQuery(criteriaQuery.multiselect(multiSelect));

        return query.getResultList();
    }


    @Cacheable(value = "locationWithProjectStats")
    public List<LocationProjectStatsDao> getLocationWithProjectStats(Parameters params) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery  criteriaQuery = criteriaBuilder.createQuery(LocationProjectStatsDao.class);
        Root<Location> locationRoot = criteriaQuery.from(Location.class);

        List<Selection<?>> multiSelect = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        List<Expression<?>> groupByList = new ArrayList<>();

        multiSelect.add(locationRoot.get(Location_.id));
        groupByList.add(locationRoot.get(Location_.id));
        multiSelect.add(locationRoot.get(Location_.name));
        groupByList.add(locationRoot.get(Location_.name));
        multiSelect.add(locationRoot.get(Location_.centroid));
        groupByList.add(locationRoot.get(Location_.centroid));

        Join<Location, ProjectLocation> projectLocationJoin = locationRoot.join(Location_.projects, JoinType.INNER); //location -> project_location
        Join<ProjectLocation, ProjectLocationId> idJoin = projectLocationJoin.join(ProjectLocation_.pk, JoinType.INNER); //
        Join<ProjectLocationId, Project> projectJoin = idJoin.join(ProjectLocationId_.project, JoinType.INNER); //project_location to project

        multiSelect.add(criteriaBuilder.countDistinct(projectJoin).alias("count"));
        multiSelect.add(criteriaBuilder.avg(projectJoin.get(Project_.physicalProgress)).alias("avg"));

        FilterHelper.filterLocationQuery(params, criteriaBuilder, locationRoot, predicates, projectJoin, null, projectLocationJoin, null);
        Predicate other = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        criteriaQuery.where(other);
        criteriaQuery.orderBy(criteriaBuilder.asc(locationRoot.get(Location_.id)));

        criteriaQuery.groupBy(groupByList);
        TypedQuery<LocationProjectStatsDao> query = em.createQuery(criteriaQuery.multiselect(multiSelect));

        return query.getResultList();
    }

    @Cacheable(value = "locationWithTransactionStats")
    public List<LocationResultsDao> getLocationWithTransactionStats(Parameters params) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery  criteriaQuery = criteriaBuilder.createQuery(LocationResultsDao.class);
        Root<Location> locationRoot = criteriaQuery.from(Location.class);

        List<Selection<?>> multiSelect = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        List<Expression<?>> groupByList = new ArrayList<>();

        multiSelect.add(locationRoot.get(Location_.id));
        groupByList.add(locationRoot.get(Location_.id));
        multiSelect.add(locationRoot.get(Location_.name));
        groupByList.add(locationRoot.get(Location_.id));
        multiSelect.add(locationRoot.get(Location_.centroid));
        groupByList.add(locationRoot.get(Location_.centroid));

        groupByList.add(locationRoot);

        Join<Location, ProjectLocation> projectLocationJoin = locationRoot.join(Location_.projects, JoinType.INNER); //location -> project_location
        Join<ProjectLocation, ProjectLocationId> idJoin = projectLocationJoin.join(ProjectLocation_.pk, JoinType.INNER); //
        Join<ProjectLocationId, Project> projectJoin = idJoin.join(ProjectLocationId_.project, JoinType.INNER); //project_location to project
        Join<Project, Transaction> transactionJoin = projectJoin.join(Project_.transactions, JoinType.LEFT); //project to transaction (left in order to be able to count projects without transactions)

        multiSelect.add(transactionJoin.get(Transaction_.transactionStatusId));
        groupByList.add(transactionJoin.get(Transaction_.transactionStatusId));

        multiSelect.add(transactionJoin.get(Transaction_.transactionTypeId));
        groupByList.add(transactionJoin.get(Transaction_.transactionTypeId));


        //add params filters
        Expression<Double> utilization;
        if(params.getLocations()!=null){
            utilization = transactionJoin.get(Transaction_.amount);
        } else {
            utilization = criteriaBuilder.prod(transactionJoin.get(Transaction_.amount), projectLocationJoin.get(ProjectLocation_.utilization));
        }
        Expression<Double> expression = FilterHelper.filterLocationQuery(params, criteriaBuilder, locationRoot, predicates, projectJoin, utilization, projectLocationJoin, transactionJoin);

        multiSelect.add(criteriaBuilder.sum(expression));

        Predicate other = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        criteriaQuery.where(other);
        criteriaQuery.orderBy(criteriaBuilder.asc(locationRoot.get(Location_.id)));

        criteriaQuery.groupBy(groupByList);
        TypedQuery<LocationResultsDao> query = em.createQuery(criteriaQuery.multiselect(multiSelect));

        return query.getResultList();
    }


    @Cacheable(value = "shapesWithDetail")
    public List<GeometryDao> getShapesByLevelAndDetail(int level, double detail) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery  criteriaQuery = criteriaBuilder.createQuery(GeometryDao.class);
        Root<Location> locationRoot = criteriaQuery.from(Location.class);

        List<Selection<?>> multiSelect = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(locationRoot.get(Location_.level).in(level));

        Join<Location, LocationGeometry> geometryJoin = locationRoot.join(Location_.locationGeometry, JoinType.LEFT);

        predicates.add(geometryJoin.get(LocationGeometry_.geometry).isNotNull());

        //multiSelect.add(geometryJoin.get(LocationGeometry_.geometry));
        ParameterExpression<Double> detailparam = criteriaBuilder.parameter(Double.class, "detail");
        Expression function=criteriaBuilder.function("ST_Simplify", Geometry.class, geometryJoin.get(LocationGeometry_.geometry), detailparam);

        multiSelect.add(locationRoot.get(Location_.id));
        multiSelect.add(locationRoot.get(Location_.name));
        multiSelect.add(function);
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));

        TypedQuery<GeometryDao> query = em.createQuery(criteriaQuery.multiselect(multiSelect));
        query.setParameter("detail", detail);
        return query.getResultList();
    }

}
