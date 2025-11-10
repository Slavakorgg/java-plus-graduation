package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentAdminServiceImpl implements CommentAdminService {

    private final CommentRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void delete(Long comId) {
        log.info("admin delete - invoked for comment ID: {}", comId);
        if (!repository.existsById(comId)) {
            log.error("Comment with id = {} not found", comId);
            throw new NotFoundException("Comment not found");
        }
        log.info("Result: comment with id = {} deleted", comId);
        repository.deleteById(comId);
    }

    @Override
    public List<CommentDto> search(String text, int from, int size) {
        log.info("admin search - invoked with text='{}', from={}, size={}", text, from, size);
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> page = repository.findAllByText(text, pageable);
        List<Comment> list = page.getContent();
        log.info("Result: found {} comments for search query '{}'", list.size(), text);
        return CommentMapper.toListCommentDto(list);
    }

    @Override
    public List<CommentDto> findAllByUserId(Long userId, int from, int size) {
        log.info("admin findAllByUserId - invoked for user ID: {}, from={}, size={}", userId, from, size);
        if (!userRepository.existsById(userId)) {
            log.error("User with id = {} not found", userId);
            throw new NotFoundException("User not found");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Comment> page = repository.findAllByAuthorId(userId, pageable);
        List<Comment> list = page.getContent();
        log.info("Result: user ID {} has {} comments", userId, list.size());
        return CommentMapper.toListCommentDto(list);
    }

    @Override
    @Transactional
    public CommentDto approveComment(Long comId) {
        log.info("approveComment - invoked for comment ID: {}", comId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        comment.setApproved(true);
        repository.save(comment);
        log.info("Result: comment with id = {} approved successfully", comId);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto rejectComment(Long comId) {
        log.info("rejectComment - invoked for comment ID: {}", comId);
        Comment comment = repository.findById(comId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        comment.setApproved(false);
        repository.save(comment);
        log.info("Result: comment with id = {} rejected successfully", comId);
        return CommentMapper.toCommentDto(comment);
    }
}