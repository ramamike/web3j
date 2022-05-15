package com.learn.task.SpringBlockChainWebDB.repository;

import com.learn.task.SpringBlockChainWebDB.constants.Constants;
import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import com.learn.task.SpringBlockChainWebDB.service.Web3jService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.List;

@SpringBootTest
class EventRepositoryTest {

    @Autowired
    public EventRepositoryTest(EventRepository eventRepository, Web3jService web3jService) {
        this.eventRepository = eventRepository;
        this.web3jService = web3jService;
    }

    private EventRepository eventRepository;
    private Web3jService web3jService;

    @Test
    public void testInsertEventList(){

        EthFilter ethFilter=web3jService.getEthFilterRequest(
                new BigInteger("18409313"),
                new BigInteger("18409372"),
                Constants.CONTRACT_ADDRESS
        );
        List<EventEntity> eventEntityList=web3jService.getEvents(ethFilter);
        eventRepository.saveAll(eventEntityList);
    }

}