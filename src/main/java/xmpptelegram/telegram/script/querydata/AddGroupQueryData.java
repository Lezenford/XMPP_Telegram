package xmpptelegram.telegram.script.querydata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddGroupQueryData extends QueryData {

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("command")
    private InlineCommandType commandType;

    @JsonProperty("data")
    private String data;

    public AddGroupQueryData(InlineCommandType commandType, String data) {
        super(QueryDataType.ADD_GROUP);
        this.commandType = commandType;
        this.data = data;
    }
}
