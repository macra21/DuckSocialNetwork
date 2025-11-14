package com.org.ddd.domain.entities;

public class FlyingDuck extends Duck implements Flyer{
    public FlyingDuck(String username, String email, String password, double speed, DuckType duckType, double resistance) {
        super(username, email, password, speed, duckType, resistance);
    }

    @Override
    public void fly() {
        System.out.println("Flying duck " + getUsername() + " is flying with speed " + getSpeed() + " and resistance " + getResistance());
    }

}
