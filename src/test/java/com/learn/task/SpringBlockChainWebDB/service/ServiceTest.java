package com.learn.task.SpringBlockChainWebDB.service;

import com.learn.task.SpringBlockChainWebDB.constants.Constants;
import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import com.learn.task.SpringBlockChainWebDB.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@SpringBootTest
@Slf4j
class ServiceTest {

    private Web3jService web3jService;
    private EventRepository eventRepository;

    @Autowired
    public ServiceTest(Web3jService web3jService, EventRepository eventRepository) {
        this.web3jService = web3jService;
        this.eventRepository=eventRepository;
    }

    @Test
    public void testLogWeb3j() throws IOException {
        web3jService.getClientVersion();
    }

    @Test
    public void testGetBalance() throws Exception{
        web3jService.getBalance();
    }

    @Test
    public void testGetEventsFilter(){

        EthFilter ethFilter=web3jService.getEthFilterRequest(
                new BigInteger("18409313"),
                new BigInteger("18409372"),
                Constants.CONTRACT_ADDRESS
                );
        web3jService.getEvents(ethFilter);

    }

    @Test
    public void testGetEvents(){

        List<EventEntity> eventEntityList=eventRepository.getEvent(false);
        System.out.println();
    }
    @Test
    public void testGetEventsWithLimit(){

        List<EventEntity> eventEntityList=eventRepository.getEventByStateWithLimit(false, 2);
        System.out.println();
    }

}