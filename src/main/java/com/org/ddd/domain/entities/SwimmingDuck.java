package com.org.ddd.domain.entities;

public class SwimmingDuck extends Duck implements Swimmer{
    public SwimmingDuck(String username, String email, String password, double speed, DuckType duckType, double resistance) {
        super(username, email, password, speed, duckType, resistance);
    }

    @Override
    public void swim() {
        System.out.println("Swimming duck " + getUsername() + " is swimming with speed " + getSpeed() + " and resistance " + getResistance());
    }

}
