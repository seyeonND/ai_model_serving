package aistudio.data.domain.dto.dataset;

import java.time.LocalDateTime;
import java.util.List;

import aistudio.data.domain.dto.data.DataFileIdRequestDTO;
import aistudio.data.domain.dto.data.DataTagRequestDTO;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DatasetGetDTO {
    private String datasetId;
    private String workspaceId;
    private String datasetName;
    private List<DataFileIdRequestDTO> fileIds;
    private List<DataTagRequestDTO> tags;
    private String registerId;
    private LocalDateTime registerDatetime;
    private String modifyId;
    private LocalDateTime modifyDatetime;
}
