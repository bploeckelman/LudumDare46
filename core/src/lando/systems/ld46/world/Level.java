package lando.systems.ld46.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.ld46.Assets;
import lando.systems.ld46.entities.BodyPart;
import lando.systems.ld46.entities.EnemyType;
import lando.systems.ld46.physics.Segment2D;
import lando.systems.ld46.screens.GameScreen;
import lando.systems.ld46.utils.Utils;

public class Level {

    public static final float TILE_SIZE = 32f;

    public enum LayerType { background, collision, foreground }
    public static class Layer {
        public final int[] index;
        public final TiledMapTileLayer tileLayer;

        public Layer(int index, TiledMapTileLayer tileLayer) {
            this.index = new int[]{ index };
            this.tileLayer = tileLayer;
        }
    }

    private Assets assets;
    private GameScreen gameScreen;

    public String name;
    public TiledMap map;
    public TiledMapRenderer renderer;
    public ObjectMap<LayerType, Layer> layers;
    public Array<Segment2D> collisionSegments;

    public MapLayer objectsLayer;
    public SpawnPlayer playerSpawn;
    public SpawnBoss bossSpawn;
    public Exit exit;
    public Array<SpawnEnemy> enemySpawns;
    public Array<PunchWall> punchWalls;
    public ObjectMap<BodyPart.Type, Vector2> initialBodyPartPositions;

    public Pool<Rectangle> rectPool = Pools.get(Rectangle.class);

    public LevelDescriptor thisLevel;
    public LevelDescriptor nextLevel = null;

    private Array<Rectangle> tileRects = new Array<>();
    private Rectangle tempRect = new Rectangle();
    public boolean segmentsDirty;

    public Level(LevelDescriptor levelDescriptor, GameScreen gameScreen) {
        Gdx.app.log("Level", "Loading: " + levelDescriptor.toString());

        this.assets = gameScreen.game.assets;
        this.gameScreen = gameScreen;
        this.thisLevel = levelDescriptor;

        // Load map
        this.map = (new TmxMapLoader()).load(levelDescriptor.mapFileName, new TmxMapLoader.Parameters() {{
            generateMipMaps = true;
            textureMinFilter = Texture.TextureFilter.MipMap;
            textureMagFilter = Texture.TextureFilter.MipMap;
        }});
        this.renderer = new OrthoCachedTiledMapRenderer(map);
        ((OrthoCachedTiledMapRenderer) renderer).setBlending(true);

        // Load map properties
        this.name = map.getProperties().get("name", "[UNNAMED]", String.class);
        String nextLevelName = map.getProperties().get("next-level", null, String.class);
        if (nextLevelName != null) {
            this.nextLevel = LevelDescriptor.valueOf(nextLevelName);
        }

        // Load and validate map layers
        MapLayers mapLayers = map.getLayers();
        this.layers = new ObjectMap<>();
        for (int i = 0; i < mapLayers.size(); ++i) {
            MapLayer mapLayer = mapLayers.get(i);
            if (mapLayer.getName().equalsIgnoreCase("objects")) {
                this.objectsLayer = mapLayer;
            } else if (mapLayer instanceof TiledMapTileLayer){
                Layer layer = new Layer(i, (TiledMapTileLayer) mapLayer);
                if      (mapLayer.getName().equalsIgnoreCase("background")) this.layers.put(LayerType.background, layer);
                else if (mapLayer.getName().equalsIgnoreCase("collision"))  this.layers.put(LayerType.collision,  layer);
                else if (mapLayer.getName().equalsIgnoreCase("foreground")) this.layers.put(LayerType.foreground, layer);
            } else {
                Gdx.app.log("Level", "Tilemap has a weird layer that is neither 'objects' nor one of the TiledMapTileLayer types: '" + mapLayer.getName() + "'");
            }
        }

        if (this.layers.get(LayerType.background) == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'background'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }
        if (this.layers.get(LayerType.collision) == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'collision'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }
        if (this.layers.get(LayerType.foreground) == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'foreground'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }
        if (this.objectsLayer == null) {
            throw new GdxRuntimeException("Tilemap missing required layer: 'objects'. (required layers: 'background', 'collision', 'foreground', 'objects')");
        }

        // Load map objects
        exit = null;
        playerSpawn = null;
        bossSpawn = null;
        enemySpawns = new Array<>();
        punchWalls = new Array<>();
        initialBodyPartPositions = new ObjectMap<>();

        MapObjects objects = objectsLayer.getObjects();
        for (MapObject object : objects) {
            MapProperties props = object.getProperties();
            String type = (String) props.get("type");
            if (type == null) {
                Gdx.app.log("Map", "Map object missing 'type' property");
                continue;
            }
            float x = props.get("x", Float.class);
            float y = props.get("y", Float.class);

            if ("spawn-player".equalsIgnoreCase(type)) {
                playerSpawn = new SpawnPlayer(x, y, assets);
            }
            else if ("spawn-enemy".equalsIgnoreCase(type)) {
                if ("boss".equalsIgnoreCase(object.getName())) {
                    bossSpawn = new SpawnBoss(gameScreen, x, y);
                } else {
                    EnemyType enemyType = EnemyType.valueOf((String) props.get("enemy-type"));
                    int maxSpawn = props.get("max-spawn", Integer.class);
                    float spawnRate = props.get("spawn-rate", Float.class);
                    SpawnEnemy spawn = new SpawnEnemy(gameScreen.game, enemyType, x, y, maxSpawn, spawnRate);
                    enemySpawns.add(spawn);
                }
            }
            else if ("exit".equalsIgnoreCase(type)) {
                exit = new Exit(x, y, assets);
            }
            else if ("punch-wall".equalsIgnoreCase(type)) {
                punchWalls.add(new PunchWall(x, y, assets));
            }
            else if ("body-part".equalsIgnoreCase(type)) {
                BodyPart.Type partType = BodyPart.Type.valueOf(object.getName());
                initialBodyPartPositions.put(partType, new Vector2(x, y));
            }
        }

        // Validate that we have required entities
        if (playerSpawn == null) {
            throw new GdxRuntimeException("Map missing required object: 'spawn-player'");
        }
        if (exit == null) {
            throw new GdxRuntimeException("Map missing required object: 'exit'");
        }

        buildCollisionBounds();
        punchWalls.forEach(wall -> addCollisionRectangle(wall.bounds));
        segmentsDirty = true;
    }

    public void update(float dt) {
        for (SpawnEnemy spawn : enemySpawns) {
            spawn.update(gameScreen, dt);
        }
        if (bossSpawn != null) {
            bossSpawn.update(dt);
        }

        // remove punch walls that have been marked for deletion and spawn a particle effect for each removed wall
        for (int i = punchWalls.size - 1; i >= 0; --i) {
            PunchWall wall = punchWalls.get(i);
            if (wall.dead) {
                removeCollisionRectangle(wall.bounds);
                gameScreen.particles.spawnPunchWallExplosion(wall.punchedDir,
                        wall.bounds);
                punchWalls.removeIndex(i);
            }
        }
    }

    public void render(LayerType layerType, OrthographicCamera camera) {
        Layer layer = layers.get(layerType);
        if (layer == null || layer.tileLayer == null || layer.index.length != 1) return;

        renderer.setView(camera);
        renderer.render(layer.index);
    }

    public void renderObjects(SpriteBatch batch) {
        for (PunchWall punchWall : punchWalls) {
            punchWall.render(batch);
        }
    }

    Color segmentColor = new Color();
    public void renderDebug(SpriteBatch batch) {
        float width = 3;
        float hue = 0;
        for (Segment2D segment : collisionSegments){
            hue += .17;
            batch.setColor(Utils.hsvToRgb(hue, 1f, 1f, segmentColor));
            batch.draw(assets.whitePixel, segment.start.x, segment.start.y - width/2f, 0, width/2f, segment.delta.len(), width, 1, 1, segment.getRotation());
            batch.draw(assets.whitePixel, segment.start.x + segment.delta.x/2, segment.start.y + segment.delta.y/2, 0,0, 10, 1, 1, 1, segment.normal.angle());
        }
        batch.setColor(Color.WHITE);

        exit.render(batch);
        enemySpawns.forEach(spawn -> spawn.render(batch));
        playerSpawn.render(batch);
    }

    public void addCollisionRectangle(Rectangle rect) {
        collisionSegments.add(new Segment2D(new Vector2(rect.x, rect.y), new Vector2(rect.x + rect.width, rect.y)));
        collisionSegments.add(new Segment2D(new Vector2(rect.x+rect.width, rect.y), new Vector2(rect.x + rect.width, rect.y + rect.height)));
        collisionSegments.add(new Segment2D(new Vector2(rect.x + rect.width, rect.y + rect.height), new Vector2(rect.x, rect.y + rect.height)));
        collisionSegments.add(new Segment2D(new Vector2(rect.x, rect.y + rect.height), new Vector2(rect.x, rect.y)));
        segmentsDirty = true;
    }

    public void removeCollisionRectangle(Rectangle rect) {
        Segment2D bottom = new Segment2D(new Vector2(rect.x, rect.y), new Vector2(rect.x + rect.width, rect.y));
        Segment2D right = new Segment2D(new Vector2(rect.x+rect.width, rect.y), new Vector2(rect.x + rect.width, rect.y + rect.height));
        Segment2D top = new Segment2D(new Vector2(rect.x + rect.width, rect.y + rect.height), new Vector2(rect.x, rect.y + rect.height));
        Segment2D left = new Segment2D(new Vector2(rect.x, rect.y + rect.height), new Vector2(rect.x, rect.y));

        removeSegment(bottom);
        removeSegment(right);
        removeSegment(top);
        removeSegment(left);
        segmentsDirty = true;
    }

    private void removeSegment(Segment2D segment){
        if (!collisionSegments.removeValue(segment, false)){
            Gdx.app.log("Collision", "need to write this to handle consolidated physics.");
        }
    }

    private void buildCollisionBounds() {
        collisionSegments = new Array<>();
        Pixmap pixmap = null;
        TiledMapTileLayer collisionLayer = layers.get(LayerType.collision).tileLayer;
        float tileWidth = collisionLayer.getTileWidth();
        // Build Edges
        for (int x = 0; x < collisionLayer.getWidth(); x++) {
            for (int y =0; y < collisionLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell == null) continue;
                TiledMapTileLayer.Cell cellRight = collisionLayer.getCell(x +1, y);
                TiledMapTileLayer.Cell cellLeft = collisionLayer.getCell(x -1, y);
                TiledMapTileLayer.Cell cellTop = collisionLayer.getCell(x, y +1);
                TiledMapTileLayer.Cell cellBottom = collisionLayer.getCell(x, y -1);
                Segment2D topSegment = null;
                Segment2D leftSegment = null;
                Segment2D bottomSegment = null;
                Segment2D rightSegment = null;
                if (cell != null && cellRight == null) {
                    rightSegment = new Segment2D((x+1) * tileWidth, y * tileWidth, (x+1) * tileWidth, (y+1) * tileWidth);
                }
                if (cell != null && cellLeft == null) {
                    leftSegment = new Segment2D((x) * tileWidth, (y+1) * tileWidth, (x) * tileWidth, (y) * tileWidth);
                }
                if (cell != null && cellTop == null) {
                    topSegment = new Segment2D((x+1) * tileWidth, (y+1) * tileWidth, (x) * tileWidth, (y+1) * tileWidth);
                }
                if (cell != null && cellBottom == null) {
                    bottomSegment = new Segment2D((x) * tileWidth, (y) * tileWidth, (x+1) * tileWidth, (y) * tileWidth);
                }
                if (pixmap == null) {
                    Texture texture = cell.getTile().getTextureRegion().getTexture();
                    if (!texture.getTextureData().isPrepared()) {
                        texture.getTextureData().prepare();
                    }
                    pixmap = texture.getTextureData().consumePixmap();
                }
                TextureRegion region = cell.getTile().getTextureRegion();
                int valueUL = pixmap.getPixel(region.getRegionX() + 2, region.getRegionY() +  2);
                int valueUR = pixmap.getPixel(region.getRegionX() + region.getRegionWidth() - 2, region.getRegionY() + 2);
                int valueLL = pixmap.getPixel(region.getRegionX() + 2, region.getRegionY() + region.getRegionHeight() - 2);
                int valueLR = pixmap.getPixel(region.getRegionX() + region.getRegionWidth() - 2, region.getRegionY() + region.getRegionHeight() - 2);
                if ((valueUL & 0x000000ff) == 0) {
                    if (bottomSegment != null) collisionSegments.add(bottomSegment);
                    if (rightSegment != null) collisionSegments.add(rightSegment);
                    collisionSegments.add(new Segment2D((x+1) * tileWidth, (y+1) * tileWidth, (x)* tileWidth, (y) * tileWidth));
                } else if ((valueUR & 0x000000ff) == 0){
                    if (bottomSegment != null) collisionSegments.add(bottomSegment);
                    if (leftSegment != null) collisionSegments.add(leftSegment);
                    collisionSegments.add(new Segment2D((x+1) * tileWidth, (y) * tileWidth, (x)* tileWidth, (y+1) * tileWidth));
                } else if ((valueLL & 0x000000ff) == 0){
                    if (topSegment != null) collisionSegments.add(topSegment);
                    if (rightSegment != null) collisionSegments.add(rightSegment);
                    collisionSegments.add(new Segment2D((x) * tileWidth, (y+1) * tileWidth, (x+1)* tileWidth, (y) * tileWidth));
                } else if ((valueLR & 0x000000ff) == 0){
                    if (topSegment != null) collisionSegments.add(topSegment);
                    if (leftSegment != null) collisionSegments.add(leftSegment);
                    collisionSegments.add(new Segment2D((x+1) * tileWidth, (y) * tileWidth, (x)* tileWidth, (y+1) * tileWidth));
                } else {
                    if (topSegment != null) collisionSegments.add(topSegment);
                    if (rightSegment != null) collisionSegments.add(rightSegment);
                    if (leftSegment != null) collisionSegments.add(leftSegment);
                    if (bottomSegment != null) collisionSegments.add(bottomSegment);
                }
            }
        }
        consolidateSegments();
    }

    private void consolidateSegments(){
        // consolidate segments
        boolean fixed = true;
        while (fixed){
            fixed = false;
            for (int i = 0; i < collisionSegments.size; i++) {
                Segment2D seg = collisionSegments.get(i);
                for (int j = collisionSegments.size - 1; j > i; j--) {
                    Segment2D next = collisionSegments.get(j);
                    if (seg.getRotation() != next.getRotation()) continue;
                    if (seg.end.epsilonEquals(next.start)) {
                        seg.setEnd(next.end);
                        collisionSegments.removeIndex(j);
                        fixed = true;
                    } else if (seg.start.epsilonEquals(next.end)){
                        seg.setStart(next.start);
                        collisionSegments.removeIndex(j);
                        fixed = true;
                    }
                }
            }
        }
    }

    public void getTiles(float startX, float startY, float endX, float endY, Array<Rectangle> tiles) {
        if (startX > endX) {
            float t = startX;
            startX = endX;
            endX = t;
        }
        if (startY > endY) {
            float t = startY;
            startY = endY;
            endY = t;
        }

        rectPool.freeAll(tiles);
        tiles.clear();

        TiledMapTileLayer collisionLayer = layers.get(LayerType.collision).tileLayer;
        int iStartX = (int) (startX / collisionLayer.getTileWidth());
        int iStartY = (int) (startY / collisionLayer.getTileHeight());
        int iEndX   = (int) (endX   / collisionLayer.getTileWidth());
        int iEndY   = (int) (endY   / collisionLayer.getTileHeight());
        for (int y = iStartY; y <= iEndY; y++) {
            for (int x = iStartX; x <= iEndX; x++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x * collisionLayer.getTileWidth(),
                            y * collisionLayer.getTileHeight(),
                            collisionLayer.getTileWidth(),
                            collisionLayer.getTileHeight());
                    tiles.add(rect);
                }
            }
        }
    }

}
