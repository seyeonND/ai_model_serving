package aistudio.data.domain.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import aistudio.data.domain.dto.dataset.DatasetFilterDTO;
import aistudio.data.domain.dto.dataset.DatasetGetDTO;
import aistudio.data.domain.dto.dataset.DatasetIdRequestDTO;
import aistudio.data.domain.dto.dataset.DatasetModifyDTO;
import aistudio.data.domain.dto.dataset.DatasetSaveDTO;
import aistudio.data.domain.model.DatasetEntity;
import aistudio.data.domain.service.DatasetService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dataset")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    @PostMapping("/insert")
    public DatasetEntity insertDataset(@RequestBody DatasetSaveDTO datasetSaveDTO) {
        return datasetService.insertDataset(datasetSaveDTO);
    }

    @PostMapping("/list")
    public List<DatasetEntity> getDatasetList(@RequestBody DatasetFilterDTO datasetFilterDTO) {
        return datasetService.getDatasetList(datasetFilterDTO);
    }

    @PostMapping("/get")
    public ResponseEntity<DatasetGetDTO> getDataset(@RequestBody DatasetIdRequestDTO datasetIdRequestDTO) {
        try {
            DatasetGetDTO dataset = datasetService.getDataset(datasetIdRequestDTO);
            return ResponseEntity.ok(dataset);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<DatasetEntity> updateDataset(@RequestBody DatasetModifyDTO datasetModifyDTO) {
        try {
            DatasetEntity updatedDataset = datasetService.updateDataset(datasetModifyDTO);
            return ResponseEntity.ok(updatedDataset);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/delete")
    public boolean deleteDataset(@RequestBody DatasetIdRequestDTO datasetIdRequestDTO) {
        return datasetService.deleteDataset(datasetIdRequestDTO);
    }
}
