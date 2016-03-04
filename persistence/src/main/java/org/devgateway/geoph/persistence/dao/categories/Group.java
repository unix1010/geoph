/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.geoph.persistence.dao.categories;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.devgateway.geoph.persistence.dao.Person;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;


/**
 * 
 * @author mpostelnicu
 *
 */
@Entity
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Group extends Category{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8451785172092014455L;
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE) @OneToMany  (mappedBy = "group",fetch = FetchType.LAZY)
	private Set<Person> persons = new HashSet<>();
	
	
	public Group(){
		super();
	}

	
	public Group(String label){
		super();
		this.label = label;
	}
	@Override
    public String toString() {
        return getLabel();
    }

	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public Set<Person> getPersons() {
		return persons;
	}


	public void setPersons(Set<Person> persons) {
		this.persons = persons;
	}

}