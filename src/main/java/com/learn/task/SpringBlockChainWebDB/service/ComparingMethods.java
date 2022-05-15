package com.learn.task.SpringBlockChainWebDB.service;

import com.learn.task.SpringBlockChainWebDB.entity.StakingEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingFromEventEntity;
import com.learn.task.SpringBlockChainWebDB.repository.StakingFromEventRepository;
import com.learn.task.SpringBlockChainWebDB.repository.StakingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComparingMethods {
    public ComparingMethods(StakingFromEventRepository stakingEventRepository,
                            StakingRepository stakingRepository,
                            EventService eventService,
                            StakingService stakingService) {
        this.stakingEventRepository=stakingEventRepository;
        this.stakingRepository = stakingRepository;
    }

    private EventService eventService;

    private StakingService stakingService;

    private StakingFromEventRepository stakingEventRepository;
    private StakingRepository stakingRepository;
    private boolean comparingFinished;

    public void setComparingFinished(boolean comparingFinished) {
        this.comparingFinished = comparingFinished;
    }

    public boolean doComparing(){

        List<StakingFromEventEntity> stakingEventList =stakingEventRepository.findAll();
        List<StakingEntity> stakingList =stakingRepository.findAll();

        if(stakingEventList.size()!=stakingList.size()) {
            comparingFinished=true;
            return false;
        }

        int countEqualRecord=0;
        for(StakingFromEventEntity stakeEvent : stakingEventList) {
            for(StakingEntity stake : stakingList) {
                if(stakeEvent.getAccount().equals(stake.getAccount())
                        && stakeEvent.getAmount().equals(stake.getAmount())
                        && stakeEvent.getStakingStatus().equals(stake.getStakingStatus())
                        && stakeEvent.getStakeIndex()==stake.getStakeIndex()
                        && stakeEvent.getStakingTypeIndex()==stake.getStakingTypeIndex()
                ){
                    countEqualRecord++;
                    break;
                }
            }
        }

        comparingFinished=true;

        return stakingList.size()==countEqualRecord;
    }

}
