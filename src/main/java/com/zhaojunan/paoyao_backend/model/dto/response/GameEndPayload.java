package com.zhaojunan.paoyao_backend.model.dto.response;

import com.zhaojunan.paoyao_backend.model.enumeration.WinReason;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GameEndPayload {

    private Integer winningTeam;
    private Map<Integer, Integer> teamScores;
    private WinReason winReason;

}
