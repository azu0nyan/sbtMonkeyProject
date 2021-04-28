package demoGame

import com.jme3.math.Vector3f
import com.jme3.scene.{Geometry, Mesh, VertexBuffer}
import com.jme3.util.BufferUtils
import org.recast4j.detour.{MeshData, NavMeshBuilder, NavMeshDataCreateParams}
import org.recast4j.recast.{InputGeomReader, PolyMesh, PolyMeshDetail, RecastBuilder, RecastBuilderConfig, RecastConfig}
import org.recast4j.recast.RecastConstants.PartitionType
import org.recast4j.recast.geom.{InputGeomProvider, SimpleInputGeomProvider}

import java.util.logging.{Level, Logger}
import scala.collection.mutable

object NavMeshGeneration {
  import org.recast4j.recast.AreaModification

  val SAMPLE_POLYAREA_TYPE_GROUND:Int = 0x0
  val SAMPLE_POLYAREA_TYPE_WATER:Int = 0x1
  val SAMPLE_POLYAREA_TYPE_ROAD:Int = 0x2
  val SAMPLE_POLYAREA_TYPE_DOOR:Int = 0x3
  val SAMPLE_POLYAREA_TYPE_GRASS:Int = 0x4
  val SAMPLE_POLYAREA_TYPE_JUMP:Int = 0x5
  val SAMPLE_POLYAREA_TYPE_WALKABLE:Int = 0x3f

  var SAMPLE_AREAMOD_WALKABLE = new AreaModification(SAMPLE_POLYAREA_TYPE_WALKABLE)
  var SAMPLE_AREAMOD_GROUND = new AreaModification(SAMPLE_POLYAREA_TYPE_GROUND)
  var SAMPLE_AREAMOD_WATER = new AreaModification(SAMPLE_POLYAREA_TYPE_WATER)
  var SAMPLE_AREAMOD_ROAD = new AreaModification(SAMPLE_POLYAREA_TYPE_ROAD)
  var SAMPLE_AREAMOD_GRASS = new AreaModification(SAMPLE_POLYAREA_TYPE_GRASS)
  var SAMPLE_AREAMOD_DOOR = new AreaModification(SAMPLE_POLYAREA_TYPE_DOOR)
  var SAMPLE_AREAMOD_JUMP = new AreaModification(SAMPLE_POLYAREA_TYPE_JUMP)

  val SAMPLE_POLYFLAGS_WALK = 0x01 // Ability to walk (ground, grass, road)

  val SAMPLE_POLYFLAGS_SWIM = 0x02 // Ability to swim (water).

  val SAMPLE_POLYFLAGS_DOOR = 0x04 // Ability to move through doors.

  val SAMPLE_POLYFLAGS_JUMP = 0x08 // Ability to jump.

  val SAMPLE_POLYFLAGS_DISABLED = 0x10 // Disabled polygon

  val SAMPLE_POLYFLAGS_ALL = 0xffff // All abilities.

  /** The xz-plane cell size to use for fields. [Limit: > 0] [Units: wu].
   * cs and ch define voxel/grid/cell size. So their values have significant side effects on all parameters defined in voxel units.
   * The minimum value for this parameter depends on the platform's floating point accuracy, with the practical minimum usually around 0.05. */
  val m_cellSize = 0.5f
  /** The y-axis cell size to use for fields. [Limit: > 0] [Units: wu].
   * cs and ch define voxel/grid/cell size. So their values have significant side effects on all parameters defined in voxel units.
   * The minimum value for this parameter depends on the platform's floating point accuracy, with the practical minimum usually around 0.05. */
  val m_cellHeight = 0.5f


  val m_agentHeight = 1.5f
  val m_agentRadius = 0.5f
  val m_agentMaxClimb = 0.9f
  val m_agentMaxSlope = 45.0f

  /**The minimum number of cells allowed to form isolated island areas. [Limit: >=0] [Units: vx].
   * Any regions that are smaller than this area will be marked as unwalkable. This is useful in removing useless regions that can sometimes form on geometry such as table tops, box tops, etc.*/
  val m_regionMinSize = 4
  /**Any regions with a span count smaller than this value will, if possible, be merged with larger regions.
   * [Limit: >=0] [Units: vx]*/
  val m_regionMergeSize = 20
  /** The maximum allowed length for contour edges along the border of the mesh. [Limit: >=0] [Units: vx].
   * Extra vertices will be inserted as needed to keep contour edges below this length. A value of zero effectively disables this feature.  */
  val m_edgeMaxLen = 12.0f
  /**The maximum distance a simplfied contour's border edges should deviate the original raw contour.
   * [Limit: >=0] [Units: vx]
   * The effect of this parameter only applies to the xz-plane. */
  val m_edgeMaxError = 1.3f

  val m_vertsPerPoly = 6

  /**Sets the sampling distance to use when generating the detail mesh.
   *(For height detail only.) [Limits: 0 or >= 0.9] [Units: wu]*/
  private val m_detailSampleDist = 6.0f
  /**The maximum distance the detail mesh surface should deviate from heightfield data.
   *  (For height detail only.) [Limit: >=0] [Units: wu]    *
   */
  private val m_detailSampleMaxError = 1.0f

  val m_partitionType: PartitionType = PartitionType.MONOTONE

  val log = Logger.getLogger("NavMeshGenerator")

  def generate(geoms:Seq[Geometry]) = {
    //val geomProvider = ToRecastMeshFormat.getGeomProvider(world)
    val startTime = System.currentTimeMillis()
    val vertexId = mutable.Map[Vector3f, Int]()
    val vertices = mutable.Buffer[Float]()
    val indices = mutable.Buffer[Int]()
    def addVertex(v:Vector3f):Unit = {
      vertices += v.x
      vertices += v.y
      vertices += v.z
    }
    for(g <- geoms){
      for(tri <- 0 until g.getMesh.getTriangleCount){
        val v1 = new Vector3f()
        val v2 = new Vector3f()
        val v3 = new Vector3f()
        g.getMesh.getTriangle(tri, v1, v2,v3)
        g.getLocalTransform.transformVector(v1, v1)
        g.getLocalTransform.transformVector(v2, v2)
        g.getLocalTransform.transformVector(v3, v3)
        val id1 = vertexId.getOrElseUpdate(v1, {
          addVertex(v1)
          vertexId.size
        })
        val id2 = vertexId.getOrElseUpdate(v2, {
          addVertex(v2)
          vertexId.size
        })
        val id3 = vertexId.getOrElseUpdate(v3, {
          addVertex(v3)
          vertexId.size
        })
        indices += id1
        indices += id2
        indices += id3
      }
    }

    val world:SimpleInputGeomProvider = new SimpleInputGeomProvider(vertices.toArray, indices.toArray)

    log.log(Level.INFO, s"""Generating geometry for ${vertices.size}  verts ${indices.size / 3} tris""")

    val recastConfig = new RecastConfig(m_partitionType, m_cellSize, m_cellHeight, m_agentHeight, m_agentRadius,
      m_agentMaxClimb, m_agentMaxSlope, m_regionMinSize, m_regionMergeSize, m_edgeMaxLen, m_edgeMaxError,
      m_vertsPerPoly, m_detailSampleDist, m_detailSampleMaxError,  SAMPLE_AREAMOD_WALKABLE)

    val recastBuilderConfig: RecastBuilderConfig = new RecastBuilderConfig(recastConfig,world.getMeshBoundsMin, world.getMeshBoundsMax)

    val recastBuilder:RecastBuilder = new RecastBuilder()
    val recastBuilderResult = recastBuilder.build(world,recastBuilderConfig)

    val polyMesh:PolyMesh = recastBuilderResult.getMesh
    for(i <- 0 until polyMesh.npolys){
      polyMesh.flags(i) = 1
    }
    val polyMeshDetail:PolyMeshDetail = recastBuilderResult.getMeshDetail
    val params:NavMeshDataCreateParams = new NavMeshDataCreateParams

    params.verts = polyMesh.verts
    params.vertCount = polyMesh.nverts
    params.polys = polyMesh.polys
    params.polyAreas = polyMesh.areas
    params.polyFlags = polyMesh.flags
    params.polyCount = polyMesh.npolys
    params.nvp = polyMesh.nvp
    params.detailMeshes = polyMeshDetail.meshes
    params.detailVerts = polyMeshDetail.verts
    params.detailVertsCount = polyMeshDetail.nverts
    params.detailTris = polyMeshDetail.tris
    params.detailTriCount = polyMeshDetail.ntris
    params.walkableHeight = m_agentHeight
    params.walkableRadius = m_agentRadius
    params.walkableClimb = m_agentMaxClimb
    params.bmin = polyMesh.bmin
    params.bmax = polyMesh.bmax
    params.cs = m_cellSize
    params.ch = m_cellHeight
    params.buildBvTree = true

    params.offMeshConVerts = new Array[Float](6)
    params.offMeshConVerts(0) = 0.1f
    params.offMeshConVerts(1) = 0.2f
    params.offMeshConVerts(2) = 0.3f
    params.offMeshConVerts(3) = 0.4f
    params.offMeshConVerts(4) = 0.5f
    params.offMeshConVerts(5) = 0.6f
    params.offMeshConRad = new Array[Float](1)
    params.offMeshConRad(0) = 0.1f
    params.offMeshConDir = new Array[Int](1)
    params.offMeshConDir(0) = 1
    params.offMeshConAreas = new Array[Int](1)
    params.offMeshConAreas(0) = 2
    params.offMeshConFlags = new Array[Int](1)
    params.offMeshConFlags(0) = 12
    params.offMeshConUserID = new Array[Int](1)
    params.offMeshConUserID(0) = 0x4567
    params.offMeshConCount = 1
    val meshData = NavMeshBuilder.createNavMeshData(params)

    log.log(Level.INFO, s"""Generated nav mesh ${params.polyCount} polys, ${params.vertCount} verts,  time ${System.currentTimeMillis() - startTime} ms.""")

    meshData
  }

  def meshDataToGeometry(m:MeshData):Mesh = {
    val mesh = new Mesh

    val vBuffer = BufferUtils.createFloatBuffer(m.verts.length)
    vBuffer.put(m.verts)
    mesh.setBuffer(VertexBuffer.Type.Position, 3, vBuffer)

    var trisIDs:mutable.Buffer[Short] = mutable.Buffer()
    for(p <- m.polys){
      for(i <- 1 to p.vertCount - 2){
        trisIDs += p.verts(0).toShort
        trisIDs += p.verts(i).toShort
        trisIDs += p.verts(i + 1).toShort
      }
    }

    val tBuffer = BufferUtils.createShortBuffer(trisIDs.length * 3)
    tBuffer.put(trisIDs.toArray)
    mesh.setBuffer(VertexBuffer.Type.Index, 1, tBuffer)
    mesh.updateBound()
    mesh
  }

}
