package com.zhaojunan.paoyao_backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response sent to a player when an error occurs.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    private String type;
    private String message;

}
