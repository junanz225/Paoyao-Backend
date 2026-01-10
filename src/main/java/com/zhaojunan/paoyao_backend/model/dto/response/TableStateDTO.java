package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableStateDTO {

    private String lastPlayedPlayerId;
    private List<String> cards;

}