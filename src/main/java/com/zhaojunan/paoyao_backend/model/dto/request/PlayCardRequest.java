package com.zhaojunan.paoyao_backend.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayCardRequest {

//    private String type; // "play_cards"
    private List<String> playedCards;

}
