package xmpptelegram.telegram.script.querydata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddAccountQueryData extends QueryData {

    @JsonProperty("data")
    private String data;

    public AddAccountQueryData(String data) {
        super(QueryDataType.ADD_ACCOUNT);
        this.data = data;
    }
}
