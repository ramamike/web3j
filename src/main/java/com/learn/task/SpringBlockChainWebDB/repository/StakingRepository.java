package com.learn.task.SpringBlockChainWebDB.repository;

import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StakingRepository extends JpaRepository<StakingEntity, Long> {

    public EventEntity findFirstByOrderByIdDesc();

}

