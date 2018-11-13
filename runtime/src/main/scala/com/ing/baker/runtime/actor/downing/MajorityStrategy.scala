package com.ing.baker.runtime.actor.downing

import akka.cluster.UniqueAddress
import org.slf4j.LoggerFactory

class MajorityStrategy extends Strategy {

  private val log = LoggerFactory.getLogger(classOf[MajorityStrategy])

  override def sbrDecision(clusterHelper: ClusterHelper): Unit = {
    if (clusterHelper.amIMember && clusterHelper.amILeader && clusterHelper.unreachables.nonEmpty) {
      log.info("I am leader: Unreachables: {}", clusterHelper.unreachables)

      val nodesToDown = this.nodesToDown(clusterHelper)
//      log.info(s"\n\n\nME: {}\n\n\n", clusterHelper.myUniqueAddress)
//      log.info(s"\n\n\nLAST CHANGED DATA: {}\n\n\n", clusterHelper.memberStatusLastChangedReadable)
//      log.info(s"\n\n\nUNSTABLE NODES: {}\n\n\n", clusterHelper.unstableMembers)
//      log.info(s"\n\n\nUNREACHABLE NODES: {}\n\n\n", clusterHelper.unreachables)
//      log.info(s"\n\n\nREAL UNREACHABLE NODES: {}\n\n\n", clusterHelper.cluster.state.unreachable)
//      log.info(s"\n\n\nNODES TO DOWN: {}\n\n\n", nodesToDown)
      log.info(s"${clusterHelper.myUniqueAddress} downing these nodes $nodesToDown")
      if (nodesToDown contains clusterHelper.myUniqueAddress) {
        // leader going down
        clusterHelper.downSelf()
        log.info("Self node left cluster: {}", clusterHelper.myUniqueAddress)
      } else {
        nodesToDown.foreach(a => clusterHelper.down(a.address))
        log.info("Nodes {} left cluster", nodesToDown)
      }
    }
  }

  private def nodesToDown(clusterHelper: ClusterHelper): Set[UniqueAddress] = {
    val unreachableMemberSize = clusterHelper.unreachables.size
    if (unreachableMemberSize * 2 == clusterHelper.members.size) { // cannot decide minority or majority? equal size?
      if (clusterHelper.unreachableUniqueAddresses contains clusterHelper.nodeWithSmallestAddress.get) {
        clusterHelper.reachableUniqueAddresses.toSet
      } else {
        clusterHelper.unreachableUniqueAddresses
      }
    } else {
      if (unreachableMemberSize * 2 < clusterHelper.members.size) { // am I in majority?
        clusterHelper.unreachableUniqueAddresses
      } else {
        clusterHelper.reachableUniqueAddresses.toSet
      }
    }
  }
}