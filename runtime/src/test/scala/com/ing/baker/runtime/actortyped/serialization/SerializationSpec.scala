package com.ing.baker.runtime.actortyped.serialization

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import akka.testkit.TestKit
import com.ing.baker.runtime.actor.process_index.{ProcessIndexProtocol, ProcessIndexProtocolGen}
import org.scalacheck.Prop.forAll
import org.scalacheck.Test.Parameters.defaultVerbose
import org.scalacheck._
import org.scalatest.FunSuiteLike
import org.scalatest.prop.Checkers
import com.ing.baker.runtime.actor.recipe_manager.RecipeManagerProtocolGen
import com.ing.baker.runtime.actor.recipe_manager.RecipeManagerProtocol
import com.ing.baker.runtime.core.RuntimeEventGen
import com.ing.baker.runtime.core.ProcessStateGen

class SerializationSpec extends TestKit(ActorSystem("BakerProtobufSerializerSpec")) with FunSuiteLike with Checkers {

  val serializer: BakerTypedProtobufSerializer =
    SerializationExtension
      .get(system)
      .serializerByIdentity(103)
      .asInstanceOf[BakerTypedProtobufSerializer]

  def checkFor[A <: AnyRef](name: String, gen: Gen[A]): Unit = {
    test(s"$name typed serialization") {
      check(forAll(gen)(m =>
        m == serializer.fromBinary(serializer.toBinary(m), serializer.manifest(m))),
        defaultVerbose.withMinSuccessfulTests(10)
      )
    }
  }

  checkFor("core.RuntimeEvent", RuntimeEventGen.gen)

  checkFor("core.ProcessState", ProcessStateGen.gen)

  checkFor("ProcessIndex.GetShardIndex", ProcessIndexProtocolGen.getShardIndex)

  checkFor("ProcessIndex.ActorCreated", ProcessIndexProtocolGen.actorCreated)

  checkFor("ProcessIndex.ActorDeleted", ProcessIndexProtocolGen.actorDeleted)

  checkFor("ProcessIndex.ActorPassivated", ProcessIndexProtocolGen.actorPassivated)

  checkFor("ProcessIndex.ActorActivated", ProcessIndexProtocolGen.actorActivated)

  checkFor("ProcessIndex.ActorMetadata", ProcessIndexProtocolGen.actorMetadata)

  checkFor("RecipeManagerProtocol.AddRecipe", RecipeManagerProtocolGen.addRecipe)

  checkFor("RecipeManagerProtocol.AddRecipeResponse", RecipeManagerProtocolGen.addRecipeResponse)

  checkFor("RecipeManagerProtocol.GetRecipe", RecipeManagerProtocolGen.getRecipe)

  checkFor("RecipeManagerProtocol.RecipeFound", RecipeManagerProtocolGen.recipeFound)

  checkFor("RecipeManagerProtocol.NoRecipeFound", RecipeManagerProtocolGen.noRecipeFound)

  checkFor("RecipeManagerProtocol.AllRecipes", RecipeManagerProtocolGen.allRecipes)

  test("RecipeManagerProtocol.GetAllRecipes typed serialization") {
    val serialized = serializer.toBinary(RecipeManagerProtocol.GetAllRecipes)
    val deserialized = serializer.fromBinary(serialized, serializer.manifest(RecipeManagerProtocol.GetAllRecipes))
    RecipeManagerProtocol.GetAllRecipes == deserialized
  }

  checkFor("RecipeManager.RecipeAdded", RecipeManagerProtocolGen.recipeAdded)

  test("ProcessIndexProtocol.GetIndex typed serialization") {
    val serialized = serializer.toBinary(ProcessIndexProtocol.GetIndex)
    val deserialized = serializer.fromBinary(serialized, serializer.manifest(ProcessIndexProtocol.GetIndex))
    ProcessIndexProtocol.GetIndex == deserialized
  }

  checkFor("ProcessIndexProtocol.Index", ProcessIndexProtocolGen.index)

  checkFor("ProcessIndexProtocol.CreateProcess", ProcessIndexProtocolGen.createProcess)

  checkFor("ProcessIndexProtocol.ProcessEvent", ProcessIndexProtocolGen.processEvent)

  checkFor("ProcessIndexProtocol.RetryBlockedInteraction", ProcessIndexProtocolGen.retryBlockedInteraction)

  checkFor("ProcessIndexProtocol.ResolveBlockedInteraction", ProcessIndexProtocolGen.resolveBlockedInteraction)

  checkFor("ProcessIndexProtocol.StopRetryingInteraction", ProcessIndexProtocolGen.stopRetryingInteraction)
}
