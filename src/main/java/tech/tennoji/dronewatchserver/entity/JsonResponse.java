package tech.tennoji.dronewatchserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonResponse<T> {
    private int code;
    private String message;
    private T data;
}
