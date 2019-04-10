package com.ing.baker.runtime.core

import cats.instances.list._
import cats.instances.try_._
import cats.syntax.traverse._
import com.ing.baker.runtime.actortyped.serialization.BinarySerializable
import com.ing.baker.types.Value
import com.ing.baker.runtime.actor.protobuf
import com.ing.baker.runtime.actortyped.serialization.ProtobufMapping.{fromProto, toProto, versioned}

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Holds the 'state' of a process instance.
  *
  * @param processId The process identifier
  * @param ingredients The accumulated ingredients
  * @param eventNames The names of the events occurred so far
  */
case class ProcessState(processId: String,
                        ingredients: Map[String, Value],
                        eventNames: List[String]) extends Serializable {

  /**
    * Returns the accumulated ingredients.
    *
    * @return The accumulated ingredients
    */
  def getIngredients(): java.util.Map[String, Value] = ingredients.asJava

  /**
    * Returns the names of the events occurred so far.
    *
    * @return The names of the events occurred so far
    */
  def getEventNames(): java.util.List[String] = eventNames.asJava

  /**
    * Returns the process identifier.
    *
    * @return The process identifier
    */
  def getProcessId(): String = processId
}

object ProcessState {

  def serializer: BinarySerializable =
    new BinarySerializable {

      type Type = ProcessState

      val tag: Class[ProcessState] = classOf[ProcessState]

      def manifest: String = "core.ProcessState"

      def toBinary(a: ProcessState): Array[Byte] = {
        val protoIngredients = a.ingredients.toSeq.map { case (name, value) =>
          protobuf.Ingredient(Some(name), None, Some(toProto(value)))
        }
        protobuf.ProcessState(Some(a.processId), protoIngredients, a.eventNames).toByteArray
      }

      def fromBinary(binary: Array[Byte]): Try[ProcessState] =
        for {
          message <- Try(protobuf.ProcessState.parseFrom(binary))
          processId <- versioned(message.processId, "processId")
          ingredients <- message.ingredients.toList.traverse[Try, (String, Value)] { i =>
            for {
              name <- versioned(i.name, "name")
              protoValue <- versioned(i.value, "value")
              value <- fromProto(protoValue)
            } yield (name, value)
          }
        } yield ProcessState(processId, ingredients.toMap, message.eventNames.toList)
    }
}