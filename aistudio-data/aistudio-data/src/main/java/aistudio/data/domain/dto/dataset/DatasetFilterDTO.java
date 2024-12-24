package aistudio.data.domain.dto.dataset;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "데이터셋 조회 필터")
public class DatasetFilterDTO {
    @Schema(description = "워크스페이스별 데이터셋 조회", required = false)
    private List<String> workspaceIds;
    @Schema(description = "내가 만든 데이터셋만 조회", required = false)
    private Boolean myDataset;
    @Schema(description = "태그 검색", required = false)
    private List<String> tags;
}
