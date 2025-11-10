package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.util.Util.createPageRequestAsc;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentPublicServiceImpl implements CommentPublicService {

    private final CommentRepository repository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto getComment(Long comId) {
        log.info("getComment - invoked for comment ID: {}", comId);

        Comment comment = repository.findById(comId)
                .orElseThrow(() -> {
                    log.error("Comment with ID {} not found", comId);
                    return new NotFoundException("Comment not found");
                });

        if (!comment.isApproved()) {
            log.warn("Comment with ID {} is not approved (current state: {})",
                    comId, comment.isApproved());
            throw new ForbiddenException("Comment is not approved");
        }

        log.info("Result: successfully retrieved approved comment with ID {}", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentShortDto> getCommentsByEvent(Long eventId, int from, int size) {
        log.info("getCommentsByEvent - invoked for event ID: {}, from: {}, size: {}",
                eventId, from, size);

        if (!eventRepository.existsById(eventId)) {
            log.error("Event with ID {} does not exist", eventId);
            throw new NotFoundException("Event not found");
        }

        Pageable pageable = createPageRequestAsc("createTime", from, size);
        Page<Comment> commentsPage = repository.findAllByEventId(eventId, pageable);
        List<Comment> comments = commentsPage.getContent();

        List<Comment> approvedComments = comments.stream()
                .filter(Comment::isApproved)
                .collect(Collectors.toList());

        log.info("Result: retrieved {} approved comments for event ID {} (requested {} items, offset {})",
                approvedComments.size(), eventId, size, from);

        return CommentMapper.toListCommentShortDto(approvedComments);
    }

    @Override
    public CommentDto getCommentByEventAndCommentId(Long eventId, Long commentId) {
        log.info("getCommentByEventAndCommentId - invoked for event ID: {}, comment ID: {}",
                eventId, commentId);

        Comment comment = repository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Comment with ID {} not found", commentId);
                    return new NotFoundException("Comment not found");
                });

        if (!comment.getEvent().getId().equals(eventId)) {
            log.error("Comment ID {} does not belong to event ID {} (belongs to event ID {})",
                    commentId, eventId, comment.getEvent().getId());
            throw new NotFoundException("Comment not found for the specified event");
        }

        if (!comment.isApproved()) {
            log.warn("Comment ID {} is not approved (cannot be accessed)", commentId);
            throw new ForbiddenException("Comment is not approved");
        }

        log.info("Result: successfully retrieved comment ID {} for event ID {}",
                commentId, eventId);

        return CommentMapper.toCommentDto(comment);
    }
}
