package com.org.ddd.domain.entities;

public class FlyingAndSwimingDuck extends Duck implements Flyer, Swimmer{
    public FlyingAndSwimingDuck(String username, String email, String password, double speed, DuckType duckType, double resistance) {
        super(username, email, password, speed, duckType, resistance);
    }

    @Override
    public void fly() {
        System.out.println("Flying and swimming duck " + getUsername() + " is flying with speed " + getSpeed() + " and resistance " + getResistance());
    }

    @Override
    public void swim() {
        System.out.println("Flying and swimming duck " + getUsername() + " is swimming with speed " + getSpeed() + " and resistance " + getResistance());
    }

}
