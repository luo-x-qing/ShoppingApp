package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Comment;
import com.example.ordermanagement.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/attraction/{attractionId}")
    public List<Comment> getComments(@PathVariable Long attractionId) {
        return commentService.getCommentsByAttractionId(attractionId);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
        return ResponseEntity.ok(commentService.saveComment(comment));
    }
}