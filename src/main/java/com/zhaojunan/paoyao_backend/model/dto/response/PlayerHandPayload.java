package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerHandPayload {

    private String playerId;
    private List<String> cards;

}
