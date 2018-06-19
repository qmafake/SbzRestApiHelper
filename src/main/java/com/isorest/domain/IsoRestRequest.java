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
public class IsoRestRequest {

    private SubIso isomsg;
    private List<Field> field;
    private String direction;
   

    public SubIso getIsomsg() {
        return isomsg;
    }

    public void setIsomsg(SubIso isomsg) {
        this.isomsg = isomsg;
    }

    public List<Field> getField() {
        return field;
    }

    public void setField(List<Field> field) {
        this.field = field;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "IsoRestRequest{" + "isomsg=" + isomsg + ", field=" + field + ", direction=" + direction +'}';
    }
}
