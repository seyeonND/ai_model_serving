package aistudio.data.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import aistudio.data.domain.model.DatasetEntity;

@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, String> {

        List<DatasetEntity> findByRegisterId(String registerId);

        @Query(value = "SELECT * FROM dataset WHERE workspace_id IN (:workspaceIds)", nativeQuery = true)
        List<DatasetEntity> findByWorkspaceIds(@Param("workspaceIds") List<String> workspaceIds);

        @Query(value = "SELECT * FROM dataset WHERE tags && ARRAY[:tags]::text[]", nativeQuery = true)
        List<DatasetEntity> findByTags(@Param("tags") String[] tags);

        @Query(value = "SELECT * FROM dataset " +
                        "WHERE workspace_id IN (:workspaceIds) " +
                        "AND register_id = :registerId", nativeQuery = true)
        List<DatasetEntity> findByWorkspaceIdsAndRegisterId(
                        @Param("workspaceIds") List<String> workspaceIds,
                        @Param("registerId") String registerId);

        @Query(value = "SELECT * FROM dataset " +
                        "WHERE workspace_id IN (:workspaceIds) " +
                        "AND tags && ARRAY[:tags]::text[]", nativeQuery = true)
        List<DatasetEntity> findByWorkspaceIdsAndTags(
                        @Param("workspaceIds") List<String> workspaceIds,
                        @Param("tags") String[] tags);

        @Query(value = "SELECT * FROM dataset " +
                        "WHERE register_id = :registerId " +
                        "AND tags && ARRAY[:tags]::text[]", nativeQuery = true)
        List<DatasetEntity> findByRegisterIdAndTags(
                        @Param("registerId") String registerId,
                        @Param("tags") String[] tags);

        @Query(value = "SELECT * FROM dataset " +
                        "WHERE workspace_id IN (:workspaceIds) " +
                        "AND register_id = :registerId " +
                        "AND tags && ARRAY[:tags]::text[]", nativeQuery = true)
        List<DatasetEntity> findByWorkspaceIdsAndRegisterIdAndTags(
                        @Param("workspaceIds") List<String> workspaceIds,
                        @Param("registerId") String registerId,
                        @Param("tags") String[] tags);

}
