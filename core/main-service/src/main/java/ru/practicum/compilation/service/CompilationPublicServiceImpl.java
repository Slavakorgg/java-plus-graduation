package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationPublicServiceImpl implements CompilationPublicService {

    private final CompilationRepository compilationRepository;

    @Override
    public CompilationDto readCompilationById(Long compId) {
        log.info("readCompilationById - invoked for compilation ID: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("Compilation with ID {} not found", compId);
                    return new NotFoundException("Compilation not found");
                });

        log.info("Result: compilation ID {} retrieved successfully (title: '{}')",
                compId, compilation.getTitle());

        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> readAllCompilations(Boolean pinned, int from, int size) {
        log.info("readAllCompilations - invoked. Pinned: {}, from: {}, size: {}",
                pinned, from, size);

        Pageable pageable = PageRequest.of(from, size, Sort.Direction.ASC, "id");
        List<Compilation> compilations = (pinned == null)
                ? compilationRepository.findAll(pageable).getContent()
                : compilationRepository.findAllByPinned(pinned, pageable);

        int resultSize = compilations.size();
        log.info("Result: retrieved {} compilations (pinned={}, from={}, size={})",
                resultSize, pinned, from, size);

        return CompilationMapper.toCompilationDtoList(compilations);
    }
}
