package com.nss.nssreport.domain.mapper;

import com.nss.nssreport.domain.dto.AsrDto;
import com.nss.nssreport.domain.entity.AsrEntity;
import org.springframework.stereotype.Component;

@Component
public class AsrMapper {

    // Конвертация DTO → Entity (перед сохранением в БД)
    public AsrEntity toEntity(AsrDto dto) {
        AsrEntity entity = new AsrEntity();
        entity.setObjectInstance(dto.getObjectInstance());
        entity.setDate(dto.getDate());
        entity.setTime(dto.getTime());
        entity.setCallAttempt(dto.getCallAttempt());
        entity.setAnswerTimes(dto.getAnswerTimes());
        entity.setAbandonBeforeRing(dto.getAbandonBeforeRing());
        entity.setAbandonAfterRing(dto.getAbandonAfterRing());
        entity.setRingedNoAnswer(dto.getRingedNoAnswer());
        entity.setUserDetBusy(dto.getUserDetBusy());
        entity.setUserBusy(dto.getUserBusy());
        entity.setInvalidAddress(dto.getInvalidAddress());
        entity.setPagingNoResponse(dto.getPagingNoResponse());
        entity.setAbsentSubscriber(dto.getAbsentSubscriber());
        entity.setServiceRestricted(dto.getServiceRestricted());
        entity.setSpecialSignal(dto.getSpecialSignal());
        entity.setCalledNoRespond(dto.getCalledNoRespond());
        entity.setAsr(dto.getAsr());
        entity.setNer(dto.getNer());
        entity.setSucAttempt(dto.getSucAttempt());
        return entity;
    }

    // Конвертация Entity → DTO (перед отправкой клиенту)
    public AsrDto toDto(AsrEntity entity) {
        AsrDto dto = new AsrDto();
        dto.setObjectInstance(entity.getObjectInstance());
        dto.setDate(entity.getDate());
        dto.setTime(entity.getTime());
        dto.setCallAttempt(entity.getCallAttempt());
        dto.setAnswerTimes(entity.getAnswerTimes());
        dto.setAsr(entity.getAsr());
        dto.setNer(entity.getNer());
        dto.setSucAttempt(entity.getSucAttempt());
        return dto;
    }
}
