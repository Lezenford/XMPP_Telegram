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
public class UpdateQueryData extends QueryData {

    @JsonProperty("id")
    private Integer accountId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonProperty("command")
    private InlineCommandType commandType;

    public UpdateQueryData(Integer accountId, InlineCommandType commandType) {
        super(QueryDataType.UPDATE_ACCOUNT);
        this.accountId = accountId;
        this.commandType = commandType;
    }
}
