package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentCreateDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.dto.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateServiceImpl implements CommentPrivateService {

    private final CommentRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentDto) {
        log.info("createComment - invoked for user ID: {}, event ID: {}", userId, eventId);

        Comment comment = CommentMapper.toComment(commentDto);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with id = {} not registered", userId);
                    return new NotFoundException("Please register first then you can comment");
                });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event with id = {} does not exist", eventId);
                    return new NotFoundException("Event not found");
                });

        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Event ID {} has state = {}, expected PUBLISHED", eventId, event.getState());
            throw new ConflictException("Event not published, you can't comment it");
        }

        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setApproved(true);
        comment.setCreateTime(LocalDateTime.now().withNano(0));

        log.info("Result: new comment created for user ID: {}, event ID: {}, comment ID: {}",
                userId, eventId, comment.getId());

        return CommentMapper.toCommentDto(repository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long comId) {
        log.info("deleteComment - invoked by user ID: {}, for comment ID: {}", userId, comId);

        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} does not exist", comId);
                    return new NotFoundException("Comment not found");
                });

        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unauthorized access: user ID {} tried to delete comment ID {}, but author is ID {}",
                    userId, comId, comment.getAuthor().getId());
            throw new ConflictException("You didn't write this comment and can't delete it");
        }

        log.info("Result: comment with id = {} deleted by user ID {}", comId, userId);
        repository.deleteById(comId);
    }

    @Override
    @Transactional
    public CommentDto patchComment(Long userId, Long comId, CommentCreateDto commentCreateDto) {
        log.info("patchComment - invoked by user ID: {}, for comment ID: {}", userId, comId);

        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with id = {} does not exist", comId);
                    return new NotFoundException("Comment not found");
                });

        if (!comment.getAuthor().getId().equals(userId)) {
            log.error("Unauthorized access: user ID {} tried to patch comment ID {}, but author is ID {}",
                    userId, comId, comment.getAuthor().getId());
            throw new ConflictException("You didn't write this comment and can't patch it");
        }

        comment.setText(commentCreateDto.getText());
        comment.setPatchTime(LocalDateTime.now().withNano(0));

        log.info("Result: comment with id = {} updated by user ID {}", comId, userId);
        return CommentMapper.toCommentDto(comment);
    }
}
