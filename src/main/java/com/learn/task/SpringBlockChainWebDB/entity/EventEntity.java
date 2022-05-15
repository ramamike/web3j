package com.learn.task.SpringBlockChainWebDB.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event")
@Builder
@Setter
@Getter
public class EventEntity {

    @Id
    @SequenceGenerator(
            name = "contractEvent_sequence",
            sequenceName = "contractEvent_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "contractEvent_sequence"
    )
    private Long id;

    @Column
    private BigInteger blockNumber;

    private String method;

    private String account;

    private String amount;

    private int stakingTypeIndex;

    private int stakeIndex;

    private StakingStatus stakingStatus;

    private boolean eventState;
}
