package com.learn.task.SpringBlockChainWebDB.constants;

import java.math.BigInteger;

public class Constants {

    public static final String BSC_RPC_ENDPOINT="https://data-seed-prebsc-1-s1.binance.org:8545/";
    public static final String CONTRACT_ADDRESS="0x58BCEac8551e37b5ec2B4DE1796A7f6a99Cdad5a";

    /*
    Links example to determine EVENT_HASH of method
    https://testnet.bscscan.com/address/0x58bceac8551e37b5ec2b4de1796a7f6a99cdad5a
    https://testnet.bscscan.com/tx/0x205ef1f9780f18b2c8c67c323cb9c261b464123c4dfd62c6d4b63436a040526e
    Logs
     */

    public static final String EVENT_HASH_DEPOSIT="0x36af321ec8d3c75236829c5317affd40ddb308863a1236d2d277a4025cccee1e";

    public static final String EVENT_HASH_WITHDROW="0x02f25270a4d87bea75db541cdfe559334a275b4a233520ed6c0a2429667cca94";

    public static final BigInteger START_BLOCK_NUMBER= new BigInteger("18409313");
}
