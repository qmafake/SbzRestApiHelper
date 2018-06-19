/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.isorest.domain;

import java.util.List;

/**
 *
 * @author Artwell Mamvura
 */
public class SubIso {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private List<Field> field;

    public List<Field> getField() {
        return field;
    }

    public void setField(List<Field> field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "SubIso{" + "id=" + id + ", field=" + field + '}';
    }

}
