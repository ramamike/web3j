package com.learn.task.SpringBlockChainWebDB.service;

import com.learn.task.SpringBlockChainWebDB.entity.StakingEntity;
import com.learn.task.SpringBlockChainWebDB.entity.StakingStatus;
import com.learn.task.SpringBlockChainWebDB.repository.StakingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class StakingService {

    public StakingService(StakingRepository repository) {
        this.repository=repository;
    }

    private StakingRepository repository;

    private String urlHttpService;

    private String contactAddress;

    private boolean firstStart=true;
    public void setUrlHttpService(String urlHttpService) {
        this.urlHttpService = urlHttpService;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public List<StakingEntity> getStakings(){

        Web3jService web3jService=new Web3jService(urlHttpService, contactAddress);

        //getting numbers of all staker's addresses from contract
        List<TypeReference<?>> outputForAddressesCount = new ArrayList<>();

        outputForAddressesCount.add(new TypeReference<Uint256>() {});

        int stakersNumbers=Integer.parseInt(
                web3jService.getEthCallResult("stakersAddressesCount",
                                new ArrayList<>(),
                                outputForAddressesCount)
                .get(0).getValue().toString()
            );

        // getting all addresses
        List<TypeReference<?>> outputForAddresses = new ArrayList<>();
        outputForAddresses.add(new TypeReference<Address>() {});

        List<String> stakersAddresses = new ArrayList<>();

        for(int i=0; i<stakersNumbers; i++ ) {
            List<Type> inputParameters= new ArrayList<>();
            inputParameters.add(new Uint256(i));

            String address=web3jService.getEthCallResult("stakersAddresses",
                            inputParameters,
                            outputForAddresses)
                    .get(0).getValue().toString();
            stakersAddresses.add(address);
        }

        // getting count of stake index to each staker's address
        HashMap<String, Integer> addressAndCountStakeIndex = new HashMap<>();
        stakersAddresses.forEach(staker->{
            List<Type> inputParameters= new ArrayList<>();
            inputParameters.add(new Address(staker));

            List<TypeReference<?>> outputParameters = new ArrayList<>();
            outputParameters.add(new TypeReference<Bool>() {});
            outputParameters.add(new TypeReference<Uint256>() {});

            Integer count=Integer.parseInt(web3jService.getEthCallResult("stakers",
                            inputParameters,
                            outputParameters)
                    .get(1).getValue().toString());
            addressAndCountStakeIndex.put(staker,count);
        });


        // getting staker's stake parameters
        List<StakingEntity> stakingEntityList=new ArrayList<>();
        stakersAddresses.forEach(staker->{
            //Input parameters to perform Query
            Integer indexMax =addressAndCountStakeIndex.get(staker);
            for (int i = 0; i <indexMax; i++) {

                List<Type> inputParameters = new ArrayList<>();
                inputParameters.add(new Address(staker));
                Long longForIndex=Long.valueOf(String.valueOf(i));
                inputParameters.add(new Uint256(longForIndex));

                //Output parameters
                List<TypeReference<?>> outputForStakersParam = new ArrayList<>();

                //  status (bool)
                outputForStakersParam.add(new TypeReference<Bool>() {
                });

                //  amount Uint256
                outputForStakersParam.add(new TypeReference<Uint256>() {
                });

                // amountAfter (not used)
                outputForStakersParam.add(new TypeReference<Uint256>() {
                });

                //  stakeType Uint256
                outputForStakersParam.add(new TypeReference<Uint256>() {
                });

                // EthCall
                List<Type> ethCallResult = web3jService.getEthCallResult("getStakerStakeParams",
                        inputParameters,
                        outputForStakersParam);

                boolean status = (ethCallResult.get(0).getValue().toString().equals("true"));
                String amount = ethCallResult.get(1).getValue().toString();
                int stakeType = Integer.parseInt(ethCallResult.get(3).getValue().toString());
                StakingEntity stakingEntity = StakingEntity.builder()
                        .account(staker)
                        .amount(amount)
                        .stakingTypeIndex(stakeType)
                        .stakeIndex(i)
                        .stakingStatus((status) ? StakingStatus.CLOSED : StakingStatus.OPENED)
                        .build();
                stakingEntityList.add(stakingEntity);
            }
        });
        return  stakingEntityList;
    }
    public void saveStakingListToDB(){

        List<StakingEntity> stakingEntityList=getStakings();

        // saving all Stakings at the first start
        if(firstStart) {
            if(repository.count() == 0) {
                repository.saveAll(stakingEntityList);
                return;
            }
            else firstStart=false;
        }
    }
}
