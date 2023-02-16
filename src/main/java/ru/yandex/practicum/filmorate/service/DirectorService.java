package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.DirectorNotFound;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorRepository directorRepository;
    private final FilmRepository filmRepository;

    public DirectorService(DirectorRepository directorRepository, FilmRepository filmRepository) {
        this.directorRepository = directorRepository;
        this.filmRepository = filmRepository;
    }

    public List<Director> getAllDirectors() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(int directorId) {
        isDirectorExist(directorId);
        return directorRepository.findById(directorId);
    }

    public Director createDirector(Director director) {
        return directorRepository.save(director);
    }

    public Director updateDirector(Director director) {
        Director director1 = getDirectorById(director.getId());
        director1.setName(director.getName());
        return createDirector(director1);
    }

    public void deleteDirector(int directorId) {
        isDirectorExist(directorId);
        directorRepository.deleteById(directorId);
    }

    private void isDirectorExist(int directorId) {
        if(!directorRepository.existsById(directorId)) {
            throw new DirectorNotFound("Director id: " + directorId);
        }
    }
}
