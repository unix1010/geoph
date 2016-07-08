package org.devgateway.geoph.persistence.repository;

import com.google.gson.Gson;
import org.devgateway.geoph.core.repositories.GeoPhotoRepository;
import org.devgateway.geoph.dao.GeoPhotoGeometryDao;
import org.devgateway.geoph.model.GeoPhotoSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dbianco
 *         created on abr 29 2016.
 */
@Service
public class DefaultGeoPhotoRepository implements GeoPhotoRepository {

    @Autowired
    EntityManager em;

    @Override
    public List<GeoPhotoSource> findAllGeoPhotoSources() {
        return em.createNamedQuery("findAllGeoPhotoSources", GeoPhotoSource.class).getResultList();
    }

    @Override
    public GeoPhotoSource findByCode(String name) {
        return em.createNamedQuery("findGeoPhotoSourcesByName", GeoPhotoSource.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    @Override
    public List<GeoPhotoGeometryDao> getGeoPhotoGeometryByKmlId(long kmlId) {
        Query q = em.createNativeQuery("SELECT gid,kmlid,name,symbolid,description,imagepath, " +
                "ST_AsGeoJSON(geom) as geoJsonObject from geophoto_geometry where kmlid=:kmlId")
                .setParameter("kmlId", kmlId);
        List<Object[]> resultList = q.getResultList();
        Gson g = new Gson();
        List<GeoPhotoGeometryDao> resp = new ArrayList<>();
        //Native queries cant be mapped to a class, only to an entity, that is why we create the DAO from objects
        for (Object[] o : resultList) {
            GeoPhotoGeometryDao helper = g.fromJson((String) o[6], GeoPhotoGeometryDao.class);
            helper.setGid(((Integer) o[0]));
            helper.setKmlId(((BigDecimal) o[1]).longValue());
            helper.setName((String) o[2]);
            helper.setSymbolId(((BigDecimal) o[3]).longValue());
            helper.setDescription((String) o[4]);
            helper.setImagePath((String) o[5]);
            resp.add(helper);
        }
        return resp;
    }
}