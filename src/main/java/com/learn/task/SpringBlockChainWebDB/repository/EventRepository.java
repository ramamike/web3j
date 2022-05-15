package com.learn.task.SpringBlockChainWebDB.repository;


import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

    public EventEntity findFirstByOrderByIdDesc();

    public List<EventEntity> findFirst2ByEventState(boolean eventState);

    @Query(
            value = "SELECT * FROM event e where e.event_state=?1",
            nativeQuery = true
    )
    public List<EventEntity> getEvent(boolean eventState);

    @Query(
            value = "SELECT * FROM event e where e.event_state=:state LIMIT :limit",
            nativeQuery = true
    )
    public List<EventEntity> getEventByStateWithLimit(@Param("state") boolean eventState,
                                                     @Param("limit") int limit);
}
