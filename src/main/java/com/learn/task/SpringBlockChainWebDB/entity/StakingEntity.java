package com.learn.task.SpringBlockChainWebDB.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "staking")
@Builder
@Setter
@Getter
public class StakingEntity {

    @Id
    @SequenceGenerator(
            name = "staking_sequence",
            sequenceName = "staking_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "staking_sequence"
    )
    private Long id;

    @Column
    private String account;

    @Column
    private String amount;

    @Column
    private int stakingTypeIndex;

    @Column
    private int stakeIndex;

    @Column
    private StakingStatus stakingStatus;

}
