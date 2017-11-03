package com.company.firebird.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import com.haulmont.cuba.core.entity.StandardEntity;
import javax.persistence.Column;

@Table(name = "FIREBIRD_CUSTOMER")
@Entity(name = "firebird$Customer")
public class Customer extends StandardEntity {
    private static final long serialVersionUID = 1793180804899793803L;

    @Column(name = "NAME")
    protected String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}