package com.keshetong.controller;

import com.keshetong.dto.ApiResponse;
import com.keshetong.dto.CourseChatRequest;
import com.keshetong.dto.CourseChatResponse;
import com.keshetong.service.CourseChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class CourseChatController {

    private final CourseChatService courseChatService;

    public CourseChatController(CourseChatService courseChatService) {
        this.courseChatService = courseChatService;
    }

    @PostMapping("/api/course_chat")
    public ResponseEntity<ApiResponse<CourseChatResponse>> chat(@RequestBody CourseChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(courseChatService.chat(request)));
    }

    @PostMapping(value = "/api/course_chat_stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody CourseChatRequest request) {
        String conversationId = courseChatService.resolveConversationId(request.getConversationId());
        request.setConversationId(conversationId);
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("conversation")
                    .data(Map.of("conversationId", conversationId), MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            emitter.completeWithError(new RuntimeException("初始化流式会话失败：" + e.getMessage(), e));
            return emitter;
        }

        courseChatService.streamChat(request).subscribe(
                chunk -> sendChunk(emitter, chunk),
                error -> completeWithError(emitter, error),
                () -> completeStream(emitter)
        );
        return emitter;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }

    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunk, new MediaType("text", "plain", StandardCharsets.UTF_8)));
        } catch (IOException e) {
            emitter.completeWithError(new RuntimeException("发送流式消息失败：" + e.getMessage(), e));
        }
    }

    private void completeWithError(SseEmitter emitter, Throwable error) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(error.getMessage(), new MediaType("text", "plain", StandardCharsets.UTF_8)));
        } catch (IOException ignored) {
            // ignore
        }
        emitter.completeWithError(error);
    }

    private void completeStream(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
        } catch (IOException ignored) {
            // ignore
        }
        emitter.complete();
    }
}
