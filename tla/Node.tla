-------------------------------- MODULE Node --------------------------------
EXTENDS Naturals, FiniteSets, TLC

VARIABLE othersState, \* current known state of others (my knowledge)
         amIMember, \* TRUE/FALSE
         nrOfOtherMembers,
         otherNodes

PrintVal(id, exp) == Print(<<id, exp>>, TRUE)

Majority == ((Cardinality(otherNodes) + 1) \div 2) + 1 \* calculate the majority number of nodes of all participating cluster nodes (including myself)

TotalUp(newState) == LET sum[S \in SUBSET otherNodes] ==
                     IF S = {} THEN 0
                     ELSE LET n == CHOOSE node \in S : TRUE
                          IN (IF newState[n] = "up" THEN 1 ELSE 0) + sum[S \ {n}]
                     IN sum[otherNodes] + 1

TypeOK == /\ othersState \in [otherNodes -> {"up", "unreachable"}]
          /\ amIMember \in {TRUE, FALSE}

UpdateMyState(newState) == IF TotalUp(newState) < Majority THEN FALSE ELSE TRUE

SetUp(n) == /\ othersState' = [othersState EXCEPT ![n] = "up"]
            /\ amIMember' = UpdateMyState(othersState')
            /\ UNCHANGED<<nrOfOtherMembers, otherNodes>>

SetUnreachable(n) == /\ othersState' = [othersState EXCEPT ![n] = "unreachable"]
                     /\ amIMember' = UpdateMyState(othersState')
                     /\ UNCHANGED<<nrOfOtherMembers, otherNodes>>

Init(n) == /\ nrOfOtherMembers = n \* excluding myself. I am always a member.
        \* READIBILITY IMPROVEMENTS:
        \* union with an empty set prints the set values like {1,2,3} instead of 1..3
        \* union with "node" lets us see <<node, 1>> <<node, 2>> records for each node.
        /\ otherNodes = {"node"} \times (1..nrOfOtherMembers \cup {})
        /\ othersState \in [otherNodes -> {"unreachable"}]
        /\ amIMember = FALSE
        /\ PrintVal("nrOfOtherMembers", nrOfOtherMembers)
        /\ PrintVal("otherNodes", otherNodes)

Next == \E n \in otherNodes : SetUp(n) \/ SetUnreachable(n)

MyStateIsConsistent == amIMember = UpdateMyState(othersState)

Invariants ==
    /\ TypeOK
    /\ MyStateIsConsistent

=============================================================================
\* Modification History
\* Last modified Thu Aug 23 16:04:03 CEST 2018 by se76ni
\* Last modified Wed Aug 22 12:14:00 CEST 2018 by bekiroguz
\* Created Wed Aug 15 12:26:52 CEST 2018 by bekiroguz