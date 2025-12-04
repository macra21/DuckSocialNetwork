package com.org.ddd.repository;

import com.org.ddd.utils.Identifiable;
import com.org.ddd.repository.converters.EntityConverter;
import com.org.ddd.repository.exceptions.RepositoryException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileRepository<E extends Identifiable<Long>> extends InMemoryRepository<E>{
    private final Path filePath;
    private final EntityConverter<E> converter;

    public FileRepository(Path filePath, EntityConverter<E> converter) {
        super();
        this.filePath = filePath;
        this.converter = converter;
        loadFromFile();
    }

    private void loadFromFile() throws RepositoryException{
        long maxId=0;
        try{
            if (Files.notExists(filePath)){
                throw new RepositoryException("File not found! The data file at " + filePath + " does not exist!\n");
            }

            try (BufferedReader reader = Files.newBufferedReader(filePath)){
                String line;
                while ((line = reader.readLine()) != null){
                    if (line.trim().isEmpty()) continue;

                    E entity = converter.fromLine(line);

                    super.add(entity);

                    if (entity.getId() > maxId){
                        maxId = entity.getId();
                    }
                }
            }
            idCounter.set(maxId);
        }catch (IOException e){
            throw new RepositoryException("Error reading file: " + filePath, e);
        }
    }

    private void saveToFile() throws RepositoryException{
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)){
            for (E entity : entities.values()){
                String line = converter.toLine(entity);
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e){
            throw new RepositoryException("Error writing to file: " + filePath, e);
        }
    }

    @Override
    public void add(E entity) throws RepositoryException{
        super.add(entity);
        saveToFile();
    }

    @Override
    public void update(E entity) throws RepositoryException {
        super.update(entity);
        saveToFile();
    }

    @Override
    public E delete(Long id) throws RepositoryException {
        E removed = super.delete(id);
        saveToFile();
        return removed;
    }
}
