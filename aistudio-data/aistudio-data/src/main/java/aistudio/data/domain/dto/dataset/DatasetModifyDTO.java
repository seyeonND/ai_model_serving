package aistudio.data.domain.dto.dataset;

import java.util.List;

import aistudio.data.domain.dto.data.DataFileIdRequestDTO;
import aistudio.data.domain.dto.data.DataTagRequestDTO;
import lombok.Getter;

@Getter
public class DatasetModifyDTO {
    private String datasetId;
    private String workspaceId;
    private String datasetName;
    private List<DataFileIdRequestDTO> fileIds;
    private List<DataTagRequestDTO> tags;
}
