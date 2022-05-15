package com.learn.task.SpringBlockChainWebDB.service;

import com.learn.task.SpringBlockChainWebDB.constants.Constants;
import com.learn.task.SpringBlockChainWebDB.entity.EventEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingStatus;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class Web3jService {

    public Web3jService(String urlHttpService, String contractAddress) {
        this.web3j = Web3j.build(new HttpService(urlHttpService));
        this.contractAddress=contractAddress;
    }

    final int DATA_CELL_SIZE=64;
    private Web3j web3j;
    private String contractAddress;

    public void getClientVersion() throws IOException {

        log.info(web3j.web3ClientVersion().send().getWeb3ClientVersion());
    }

    public BigInteger getBalance() throws IOException {

        EthGetBalance ethGetBalance = web3j.ethGetBalance(contractAddress, DefaultBlockParameterName.PENDING).send();
        BigInteger balance = ethGetBalance.getBalance();

        System.out.println("address " + contractAddress + " balance " + balance + " BNB");
        return balance;
    }

    public EthFilter getEthFilterRequest(BigInteger startBlock, BigInteger endBlock, String contractAddress){

        return new EthFilter(new DefaultBlockParameterNumber(startBlock),
                new DefaultBlockParameterNumber(endBlock),
                contractAddress);
    }

    public List<EventEntity> getEvents(EthFilter ethFilter){
        EthLog result=null;
        try {
            result = web3j.ethGetLogs(ethFilter).send();
        } catch (IOException e) {
            log.info("Exception", e);
            return null;
            //ToDo return eventEntityList (https://www.baeldung.com/vavr-either#2-with-either)
        }

        Response.Error error=result.getError();
        if(error!=null) {
            log.info(error.getMessage());
        }
        List<EthLog.LogResult> events=result.getLogs();
        List<EventEntity> eventEntityList=new ArrayList<>();
        events.forEach(event->{
            EthLog.LogObject logObject=(EthLog.LogObject)event;
            Log logEvent=logObject.get();
            List<String> topicList=logEvent.getTopics();
            String data=logEvent.getData();
            String clearData=data.substring(2); // remove "0x" symbols from data
            String eventHash = topicList.get(0);
            if(eventHash.equals(Constants.EVENT_HASH_DEPOSIT)
                    || eventHash.equals(Constants.EVENT_HASH_WITHDROW))
            {
                String method=eventHash.equals(Constants.EVENT_HASH_WITHDROW) ? "withdraw":"deposit";
                Address accountAddress= (Address)FunctionReturnDecoder
                        .decodeIndexedValue(clearData.substring(0, DATA_CELL_SIZE),
                                new TypeReference<Address>() { });
                Uint256 amount=(Uint256)FunctionReturnDecoder
                        .decodeIndexedValue(clearData.substring(DATA_CELL_SIZE, DATA_CELL_SIZE*2),
                                new TypeReference<Uint256>() { });
                Uint256 stakingTypeIndex=(Uint256)FunctionReturnDecoder
                        .decodeIndexedValue(clearData.substring(DATA_CELL_SIZE*2, DATA_CELL_SIZE*3),
                                new TypeReference<Uint256>() { });
                Uint256 stakeIndex=(Uint256)FunctionReturnDecoder
                        .decodeIndexedValue(clearData.substring(DATA_CELL_SIZE*3),
                                new TypeReference<Uint256>() { });


                EventEntity eventEntity =EventEntity.builder()
                        .blockNumber(logObject.get().getBlockNumber())
                        .method(method)
                        .account(accountAddress.toString())
                        .amount(amount.getValue().toString())
                        .stakingTypeIndex(stakingTypeIndex.getValue().intValue())
                        .stakeIndex(stakeIndex.getValue().intValue())
                        .stakingStatus(StakingStatus.OPENED)
                        .eventState(false)
                        .build();
                eventEntityList.add(eventEntity);
            }

            else {
               log.info("Unknown event hash");
            }

        });
        return eventEntityList;
    }


    public List<Type> getEthCallResult(String methodName,
                                       List<Type> inputParameters,
                                       List<TypeReference<?>> outputParameters) {
        //start if contract address is set.
        if(contractAddress==null){
            log.info("Contract address is not set");
            return null;
        }

        String emptyAddress = "0x0000000000000000000000000000000000000000";

        Function function = new Function(methodName, inputParameters, outputParameters);

        String encodedFunction = FunctionEncoder.encode(function);

        Transaction transaction= Transaction.
                createEthCallTransaction(emptyAddress, contractAddress, encodedFunction);

        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                    .sendAsync().get();
        } catch (InterruptedException e) {
            log.error(" Interrupted exception ", e);
            return null;
        } catch (ExecutionException e) {
            log.error(" Execution exception ", e);
            return null;
        } finally {
            log.info("EthCall.error: " + (ethCall.hasError()? ethCall.getError():"no error" ));
        }

        return FunctionReturnDecoder
                .decode(ethCall.getValue(), function.getOutputParameters());
    }

}
/*
EthLog.LogResult{
 removed=false,
 logIndex='0xd',
 transactionIndex='0x5',
 transactionHash='0x86335e87ef305c0948522b97d8984018f378c614cb176c791b9c551c914587fb',
 blockHash='0x298e518a91825632d24f415c4dcd078e8ba0d1ec0230bf863fe379b18981e806',
 blockNumber='0x118e761',
 address='0x58bceac8551e37b5ec2b4de1796a7f6a99cdad5a',
 data='0x',
 type='null',
 topics=[0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0, 0x0000000000000000000000000000000000000000000000000000000000000000, 0x0000000000000000000000003552cb128b2c3a789a16c8f244edc6a64fe3ee93]}
*/

/*
Log{removed=false,
 logIndex='0xd',
 transactionIndex='0x5',
 transactionHash='0x86335e87ef305c0948522b97d8984018f378c614cb176c791b9c551c914587fb',
 blockHash='0x298e518a91825632d24f415c4dcd078e8ba0d1ec0230bf863fe379b18981e806',
 blockNumber='0x118e761',
 address='0x58bceac8551e37b5ec2b4de1796a7f6a99cdad5a',
 data='0x',
 type='null',
 topics=[0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0,
        0x0000000000000000000000000000000000000000000000000000000000000000,
        0x0000000000000000000000003552cb128b2c3a789a16c8f244edc6a64fe3ee93]
 }
 */

/*
Data=0x
    value=[48, 120]
    coder=0
    hash=0
 */