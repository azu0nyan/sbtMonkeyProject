package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.scene.Geometry
import org.recast4j.detour.{DefaultQueryFilter, FindNearestPolyResult, NavMesh, NavMeshQuery, Result}

import scala.jdk.CollectionConverters.CollectionHasAsScala

class Navigation(seq: Seq[Geometry], showDebug:Boolean = true)(implicit app: SimpleApplication) {
  val meshData = NavMeshGeneration.generate(seq)
  val navMeshMesh = NavMeshGeneration.meshDataToGeometry(meshData)
  val navMeshGeom = new Geometry("navMesh", navMeshMesh)
  if(showDebug) {
    navMeshGeom.setMaterial(MakerUtils.makeWireframe(ColorRGBA.Cyan))
    app.getRootNode.attachChild(navMeshGeom)
  }
  val navMesh = new NavMesh(meshData, NavMeshGeneration.m_vertsPerPoly, 1)

  def findNearestPoly(p: Vector3f, extents: Vector3f = new Vector3f(10, 10, 10)): Result[FindNearestPolyResult] = {
    val query: NavMeshQuery = new NavMeshQuery(navMesh)
    query.findNearestPoly(p.toArray(Array.ofDim[Float](3)),
      extents.toArray(Array.ofDim[Float](3)), new DefaultQueryFilter())
  }
  def findPath(from: Vector3f, to: Vector3f): Option[Seq[Vector3f]] = {
    val startPolyRes = findNearestPoly(from)
    val endPolyRes = findNearestPoly(to)
    Option.when(startPolyRes.status.isSuccess && endPolyRes.status.isSuccess) {
      val query = new NavMeshQuery(navMesh)
      val pathCorridor = query.findPath(
        startPolyRes.result.getNearestRef, endPolyRes.result.getNearestRef,
        startPolyRes.result.getNearestPos, endPolyRes.result.getNearestPos, new DefaultQueryFilter()
      )
      Option.when(pathCorridor.status.isSuccess) {
        val straightPath = query.findStraightPath(
          startPolyRes.result.getNearestPos,
          endPolyRes.result.getNearestPos, pathCorridor.result, 255, NavMeshQuery.DT_STRAIGHTPATH_AREA_CROSSINGS)
        Option.when(straightPath.status.isSuccess) {
          straightPath.result.asScala.map(v => new Vector3f(v.getPos()(0), v.getPos()(1), v.getPos()(2))).toSeq
        }
      }.flatten
    }.flatten
  }
}
