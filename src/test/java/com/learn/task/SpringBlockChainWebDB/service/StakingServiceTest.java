package com.learn.task.SpringBlockChainWebDB.service;

import com.learn.task.SpringBlockChainWebDB.constants.Constants;
import com.learn.task.SpringBlockChainWebDB.repository.StakingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@Slf4j
class StakingServiceTest {

    @Autowired
    public StakingServiceTest(StakingRepository stakingRepository) {
        this.stakingRepository=stakingRepository;
    }
    private StakingRepository stakingRepository;


    private Web3j web3j = Web3j.build(new HttpService(Constants.BSC_RPC_ENDPOINT));

    @Test
    public void getStakersAddressesCount() throws ExecutionException, InterruptedException {

        String methodName="stakersAddressesCount";
        String emptyAddr = "0x0000000000000000000000000000000000000000";

        String symbol = null;

        List<Type> inputParameters=new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {};

        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String encodedFunction = FunctionEncoder.encode(function);

        Transaction transaction= Transaction.
                createEthCallTransaction(emptyAddr, Constants.CONTRACT_ADDRESS, encodedFunction);

        EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        log.info("EthCall.error: " + (ethCall.hasError()? ethCall.getError():"no error" ));

        List<Type> results= FunctionReturnDecoder
                .decode(ethCall.getValue(), function.getOutputParameters());
        log.info("results______________________________");
        symbol=results.get(0).getValue().toString();
        log.info("End_______________________________");
    }

}