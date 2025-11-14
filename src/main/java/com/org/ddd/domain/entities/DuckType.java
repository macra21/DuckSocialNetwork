package com.org.ddd.domain.entities;

public enum DuckType {
    FLYING,
    SWIMMING,
    FLYING_AND_SWIMMING;

    public boolean canFly(){
        return this == FLYING || this == FLYING_AND_SWIMMING;
    }

    public boolean canSwim(){
        return this == SWIMMING || this == FLYING_AND_SWIMMING;
    }
}
