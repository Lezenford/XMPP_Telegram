package xmpptelegram.telegram.script.querydata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
abstract class QueryData {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("type")
    private QueryDataType queryDataType;
}
