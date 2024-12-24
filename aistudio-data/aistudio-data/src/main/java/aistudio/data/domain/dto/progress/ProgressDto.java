package aistudio.data.domain.dto.progress;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressDto {
    private int countIndex;
    private int totalCount;

    public double getProgressRate() {
        if (totalCount == 0) {
            return 0; // total_count가 0일 때는 progress_rate를 0으로 설정
        }
        return (double) countIndex / totalCount * 100;
    }
}
