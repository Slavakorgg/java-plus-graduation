package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminServiceImpl implements CompilationAdminService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto request) {
        log.info("createCompilation - invoked. Title: '{}', pinned: {}, eventCount: {}",
                request.getTitle(), request.getPinned(),
                (request.getEvents() != null ? request.getEvents().size() : 0));

        Set<Event> events = (request.getEvents() != null && !request.getEvents().isEmpty())
                ? new HashSet<>(eventRepository.findAllById(request.getEvents()))
                : new HashSet<>();

        Compilation compilation = Compilation.builder()
                .pinned(request.getPinned() != null && request.getPinned())
                .title(request.getTitle())
                .events(events)
                .build();

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Result: compilation created with ID: {}, title: '{}'",
                savedCompilation.getId(), savedCompilation.getTitle());
        return CompilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        log.info("deleteCompilation - invoked for compilation ID: {}", compId);

        if (!compilationRepository.existsById(compId)) {
            log.error("Compilation with ID {} not found", compId);
            throw new NotFoundException("Compilation not found");
        }

        compilationRepository.deleteById(compId);
        log.info("Result: compilation with ID {} deleted successfully", compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        log.info("updateCompilation - invoked for ID: {}. Changes - title: {}, pinned: {}, eventCount: {}",
                compId,
                updateCompilationDto.getTitle(),
                updateCompilationDto.getPinned(),
                (updateCompilationDto.getEvents() != null ? updateCompilationDto.getEvents().size() : "unchanged"));

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Compilation with ID {} not found", compId);
                    return new NotFoundException("Compilation not found");
                });

        if (updateCompilationDto.getTitle() != null) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
            HashSet<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationDto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        log.info("Result: compilation ID {} updated successfully. New title: '{}', pinned: {}, eventCount: {}",
                updatedCompilation.getId(),
                updatedCompilation.getTitle(),
                updatedCompilation.getPinned(),
                updatedCompilation.getEvents().size());

        return CompilationMapper.toCompilationDto(updatedCompilation);
    }
}
