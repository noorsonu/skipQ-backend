package in.skipQ.template.mapper;

import in.skipQ.template.dto.BroadcastResponseDto;
import in.skipQ.template.dto.NotificationTemplateResponseDto;
import in.skipQ.template.model.BroadcastAnnouncement;
import in.skipQ.template.model.NotificationTemplate;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationTemplateMapper {

    NotificationTemplateResponseDto toDto(NotificationTemplate template);

    List<NotificationTemplateResponseDto> toDtoList(List<NotificationTemplate> templates);

    BroadcastResponseDto toBroadcastDto(BroadcastAnnouncement announcement);
}

