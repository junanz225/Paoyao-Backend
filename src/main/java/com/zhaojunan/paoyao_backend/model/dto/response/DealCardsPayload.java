package com.zhaojunan.paoyao_backend.model.dto.response;

import com.zhaojunan.paoyao_backend.model.entity.Card;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DealCardsPayload {

    private String playerId;
    private List<Card> cards;

}
