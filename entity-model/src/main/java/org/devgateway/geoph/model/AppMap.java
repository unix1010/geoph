package org.devgateway.geoph.model;

import org.devgateway.geoph.converter.StringJsonUserType;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Date;

/**
 * @author dbianco
 *         created on abr 20 2016.
 */
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@TypeDefs( {@TypeDef( name= "StringJsonObject", typeClass = StringJsonUserType.class)})
public class AppMap extends GenericPersistable implements Serializable {

    private String name;

    @Type(type = "text")
    private String description;

    private String key;

    private String md5;

<<<<<<< .merge_file_a00020

    @Type(type = "text")
    private String base64preview;

    public String getBase64preview() {
        return base64preview;
    }

    public void setBase64preview(String base64preview) {
        this.base64preview = base64preview;
    }
=======
    private String type;
>>>>>>> .merge_file_a00476

    @Type(type = "StringJsonObject")
    private String jsonAppMap;

    private Date creationDate;

    public AppMap(){

    }

<<<<<<< .merge_file_a00020
    public AppMap(String name, String description, String jsonAppMap, String key, String md5,String base64) {
=======
    public AppMap(String name, String description, String jsonAppMap, String key, String md5, String type) {
>>>>>>> .merge_file_a00476
        this.name = name;
        this.description = description;
        this.jsonAppMap = jsonAppMap;
        this.creationDate = new Date();
        this.key = key;
        this.md5 = md5;
<<<<<<< .merge_file_a00020
        this.base64preview=base64;
=======
        this.type = type;
>>>>>>> .merge_file_a00476
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getJsonAppMap() {
        return jsonAppMap;
    }

    public void setJsonAppMap(String jsonAppMap) {
        this.jsonAppMap = jsonAppMap;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
