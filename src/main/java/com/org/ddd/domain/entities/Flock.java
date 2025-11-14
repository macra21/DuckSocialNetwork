package com.org.ddd.domain.entities;

public class Flock extends Group<Duck>{
    private FlockPurpose flockPurpose;
    public Flock(String name, FlockPurpose flockPurpose) {
        super(name);
        this.flockPurpose = flockPurpose;
    }

    public FlockPurpose getFlockPurpose() {
        return flockPurpose;
    }

    @Override
    public String toString() {
        return "Flock{" +
                "id= " + getId() +
                ", name='" + getName() +
                "', memberIds=" + getMemberIds() +
                ", createdAt=" + getCreationTime() +
                '}';
    }
}
