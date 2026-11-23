package in.skipQ.template.service;

import in.skipQ.template.dto.*;
import in.skipQ.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationTemplateService {

    List<NotificationTemplateResponseDto> listTemplates();

    NotificationTemplateResponseDto getTemplateById(UUID id);

    NotificationTemplateResponseDto upsertTemplate(UpsertTemplateRequest request, User admin);

    RenderedTemplateDto previewOrTestTemplate(RenderTemplateRequest request, User admin);

    BroadcastResponseDto sendBroadcast(BroadcastRequest request, User admin);

    String renderString(String template, Map<String, String> variables);
}

