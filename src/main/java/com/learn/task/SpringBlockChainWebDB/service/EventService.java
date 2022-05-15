package com.learn.task.SpringBlockChainWebDB.service;

import com.learn.task.SpringBlockChainWebDB.constants.Constants;
import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingFromEventEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingStatus;
import com.learn.task.SpringBlockChainWebDB.repository.EventRepository;
import com.learn.task.SpringBlockChainWebDB.repository.StakingFromEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.List;


@Slf4j
@Service
@EnableScheduling
public class EventService {

    public EventService(EventRepository eventRepository,
                        StakingFromEventRepository stakingRepository) {
        this.eventRepository = eventRepository;
        this.stakingRepository = stakingRepository;
    }
    private String urlHttpService;

    private String contactAddress;

    public String getContactAddress() {
        return contactAddress;
    }

    private EventRepository eventRepository;
    private StakingFromEventRepository stakingRepository;

    private boolean schedulingActive = false;

    private BigInteger startBlockNumber = new BigInteger("18409313");
    private BigInteger lastBlockNumber = new BigInteger("18584961");

    private BigInteger startBlock = new BigInteger("0");
    private final BigInteger scanRange = new BigInteger("5000");

    private boolean scanFinished;
    private boolean checkingFinished;

    public boolean isCheckingFinished() {
        return checkingFinished;
    }

    public void setUrlHttpService(String urlHttpService) {
        this.urlHttpService = urlHttpService;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }
    public void setCheckingFinished(boolean checkingFinished) {
        this.checkingFinished = checkingFinished;
    }

    public boolean isScanFinished() {
        return scanFinished;
    }

    public void setScanFinished(boolean scanFinished) {
        this.scanFinished = scanFinished;
    }

    @Scheduled(fixedRate = 5000)
    public void scanAndSaveEvents(){

        if(scanFinished
                || urlHttpService==null || contactAddress==null
                || urlHttpService.equals("") || contactAddress.equals("")) {
            return;
        }

        Web3jService web3jService= new Web3jService(urlHttpService, contactAddress);

        BigInteger lastBlockFromRep =null;
        if(!schedulingActive) {
            EventEntity eventEntity = eventRepository.findFirstByOrderByIdDesc();
            lastBlockFromRep = (eventEntity != null) ? eventEntity.getBlockNumber() : null;
        }
        //startBlock initialising
        if (!schedulingActive && lastBlockFromRep!=null){
            startBlock=lastBlockFromRep.add(new BigInteger("1"));
        } else if (!schedulingActive) {
            startBlock=Constants.START_BLOCK_NUMBER;
        }

        // set schedulingActive if method was active once
        schedulingActive=true;
        BigInteger endBlock=startBlock.add(scanRange);

        EthFilter ethFilter=web3jService.getEthFilterRequest(
                startBlock,
                endBlock,
                contactAddress
        );
        List<EventEntity> eventEntityList=web3jService.getEvents(ethFilter);
        eventRepository.saveAll(eventEntityList);

        startBlock=endBlock.add(new BigInteger("1"));

        if(startBlock.compareTo(lastBlockNumber)>0){
            scanFinished=true;
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkingStakingStatus(){
        if(checkingFinished
                || urlHttpService==null || contactAddress==null
                || urlHttpService.equals("") || contactAddress.equals("")) {
            return;
        }

        List<EventEntity> eventEntities=eventRepository.getEventByStateWithLimit(false, 2);

        eventEntities.forEach(event ->{
            event.setStakingStatus(event.getMethod()
                    .equals("withdraw") ? StakingStatus.CLOSED : StakingStatus.OPENED);
            StakingFromEventEntity staking=StakingFromEventEntity.builder()
                    .account(event.getAccount())
                    .stakingTypeIndex(event.getStakingTypeIndex())
                    .stakeIndex(event.getStakeIndex())
                    .stakingStatus(event.getStakingStatus())
                    .build();

            StakingFromEventEntity stakingDB = stakingRepository
                    .findByAccountAndStakeIndex(staking.getAccount(), staking.getStakeIndex());

            // calculating amount
            BigInteger amountDB = new BigInteger("0");
            if(stakingDB!=null){
                amountDB=(new BigInteger(stakingDB.getAmount()));
                stakingRepository.delete(stakingDB);
            }

            BigInteger amount= new BigInteger(event.getAmount());
            if(event.getMethod().equals("withdraw")) {
                amount = amountDB;
            } else {
                amount=amount.add(amountDB);
            }
            staking.setAmount(amount.toString());


            stakingRepository.save(staking);

            event.setEventState(true);
        });

        eventRepository.saveAll(eventEntities);

        if(scanFinished){
            checkingFinished=true;
        }
    }
}
