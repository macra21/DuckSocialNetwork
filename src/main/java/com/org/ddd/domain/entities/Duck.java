package com.org.ddd.domain.entities;

public class Duck extends User{
    private double speed;
    private double resistance;
    private DuckType duckType;
    private Long flockId;

    public Duck(String username, String email, String password, double speed, double resistance, DuckType duckType) {
        super(username, email, password);
        this.speed = speed;
        this.resistance = resistance;
        this.duckType = duckType;
        this.flockId = null;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public DuckType getDuckType() {
        return duckType;
    }

    public Long getFlockId() {
        return flockId;
    }

    public void setFlockId(Long flockId) {
        this.flockId = flockId;
    }

    @Override
    public String toString() {
        return "Duck{" +
                "id=" + getId() +
                ", username='" + getUsername() +
                ", speed=" + speed +
                ", resistance=" + resistance +
                ", flockId=" + flockId +
                '}';
    }
}
