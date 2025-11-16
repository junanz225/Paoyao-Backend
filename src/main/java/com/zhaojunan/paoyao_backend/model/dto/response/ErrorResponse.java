package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response sent to a player when an error occurs.
 */
@Data
@Builder
public class ErrorResponse {

    private String type;
    private String message;

}
