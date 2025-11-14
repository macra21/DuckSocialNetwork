package com.org.ddd.repository.converters;

import com.org.ddd.domain.entities.Flock;
import com.org.ddd.domain.entities.FlockPurpose;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.time.LocalDateTime;

public class FlockConverter implements EntityConverter<Flock>{
    private static final String SEPARATOR = ":::";
    private static final String MEMBER_SEPARATOR = ",";

    @Override
    public String toLine(Flock entity) {
        StringBuilder memberIds = new StringBuilder();
        for (Long memberId : entity.getMemberIds()) {
            memberIds.append(memberId).append(MEMBER_SEPARATOR);
        }
        if (!memberIds.isEmpty()) {
            memberIds.setLength(memberIds.length() - MEMBER_SEPARATOR.length());
        }

        return entity.getId() + SEPARATOR +
                entity.getName() + SEPARATOR +
                memberIds.toString() + SEPARATOR +
                entity.getCreationTime() + SEPARATOR +
                entity.getFlockPurpose().name();
    }

    @Override
    public Flock fromLine(String line) {
        String[] parts = line.split(SEPARATOR);

        if (parts.length != 5) {
            throw new RepositoryException("Corrupt line or invalid format in file.\n");
        }

        Long id = Long.parseLong(parts[0]);
        String name = parts[1];
        String memberIdsString = parts[2];
        LocalDateTime createdAt = LocalDateTime.parse(parts[3]);
        FlockPurpose purpose = FlockPurpose.valueOf(parts[4]);

        Flock flock = new Flock(name, purpose);

        flock.setId(id);
        flock.setCreationTime(createdAt);

        if (!memberIdsString.isEmpty()) {
            String[] memberIdStrings = memberIdsString.split(MEMBER_SEPARATOR);
            for (String memberIdStr : memberIdStrings) {
                flock.addMemberId(Long.parseLong(memberIdStr));
            }
        }

        return flock;

    }
}
