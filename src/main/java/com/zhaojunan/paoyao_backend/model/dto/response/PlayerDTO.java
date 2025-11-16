package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerDTO {

    private String playerId;
    private String playerName;

}
