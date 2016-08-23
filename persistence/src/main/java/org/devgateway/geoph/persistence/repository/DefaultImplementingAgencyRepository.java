package org.devgateway.geoph.persistence.repository;

import org.devgateway.geoph.core.repositories.ImplementingAgencyRepository;
import org.devgateway.geoph.core.request.Parameters;
import org.devgateway.geoph.model.ImplementingAgency;
import org.devgateway.geoph.model.Project;
import org.devgateway.geoph.model.ProjectAgency;
import org.devgateway.geoph.model.Project_;
import org.devgateway.geoph.persistence.util.FilterHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dbianco
 *         created on abr 04 2016.
 */
@Service
public class DefaultImplementingAgencyRepository implements ImplementingAgencyRepository {

    @Autowired
    EntityManager em;

    @Override
    public List<ImplementingAgency> findAll() {
        return em.createNamedQuery("findAllImplementingAgency", ImplementingAgency.class).getResultList();
    }

    @Override
    public ImplementingAgency findById(Long id) {
        return em.createNamedQuery("findImplementingAgencyById", ImplementingAgency.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public Integer countAll() {
        return ((BigInteger) em.createNativeQuery("select count(*) from agency a where a.discriminator like 'implementing_agency'").getSingleResult()).intValue();
    }

    @Override
    public List<ProjectAgency> findFundingByImplementingAgency(Parameters params) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ProjectAgency> criteriaQuery = criteriaBuilder.createQuery(ProjectAgency.class);

        Root<Project> projectRoot = criteriaQuery.from(Project.class);

        List<Selection<?>> multiSelect = new ArrayList<>();
        List<Predicate> predicates = new ArrayList<>();
        List<Expression<?>> groupByList = new ArrayList<>();

        Join<Project, ProjectAgency> agencyJoin = projectRoot.join(Project_.implementingAgencies);
        multiSelect.add(agencyJoin);
        //multiSelect.add(projectRoot);
        groupByList.add(agencyJoin);
        //groupByList.add(projectRoot);

        FilterHelper.filterProjectQuery(params, criteriaBuilder, projectRoot, predicates);

        Predicate other = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        criteriaQuery.where(other);


        criteriaQuery.groupBy(groupByList);
        TypedQuery<ProjectAgency> query = em.createQuery(criteriaQuery.multiselect(multiSelect));

        return query.getResultList();
    }


}
