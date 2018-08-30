/* Copyright 2009-2018 EPFL, Lausanne */

package stainless
package extraction
package utils

import inox.utils.{Position, NoPosition}

object DebugSectionPositions extends inox.DebugSection("positions")

/** Inspect trees, detecting missing positions. */
object PositionChecker {

  def apply(phaseName: String)(tr: ast.Trees)(context: inox.Context): tr.TreeTraverser { val trees: tr.type } = new tr.TreeTraverser {
    val trees: tr.type = tr
    import trees._

    private implicit val debuSection = DebugSectionPositions

    private var lastKnownPosition: Position = NoPosition

    override def traverse(fd: FunDef): Unit = {
      if (fd.flags.contains(Synthetic)) return ()
      traverse(fd.id)
      fd.tparams.foreach(traverse)
      fd.params.foreach(traverse)
      traverse(fd.returnType)
      traverse(fd.fullBody)
      fd.flags.foreach(traverse)
    }

    override def traverse(e: Expr): Unit = {
      if (!e.getPos.isDefined) {
        context.reporter.debug(NoPosition, s"Missing position for expression '$e' (of type ${e.getClass}) after phase '$phaseName'. Last known position: $lastKnownPosition")
      } else {
        lastKnownPosition = e.getPos
      }

      super.traverse(e)
    }
  }

}
