package com.learn.task.SpringBlockChainWebDB.repository;

import com.learn.task.SpringBlockChainWebDB.entity.StakingFromEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StakingFromEventRepository extends JpaRepository<StakingFromEventEntity, Long> {

    public StakingFromEventEntity findByAccountAndStakeIndex(String account, int stakeIndex);

}

