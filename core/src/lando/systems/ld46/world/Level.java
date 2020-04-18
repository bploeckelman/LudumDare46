package lando.systems.ld46.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import lando.systems.ld46.Config;
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
    private GameScreen screen;

    public String name;
    public TiledMap map;
    public TiledMapRenderer renderer;
    public ObjectMap<LayerType, Layer> layers;
    public Array<Segment2D> collisionSegments;

    public MapLayer objectsLayer;
    public SpawnPlayer playerSpawn;
    public Array<SpawnEnemy> enemySpawns;
    public Exit exit;

    public Pool<Rectangle> rectPool = Pools.get(Rectangle.class);

    private Array<Rectangle> tileRects = new Array<>();
    private Rectangle tempRect = new Rectangle();

    public Level(LevelDescriptor levelDescriptor, GameScreen screen) {
        Gdx.app.log("Level", "Loading: " + levelDescriptor.toString());

        this.assets = screen.game.assets;
        this.screen = screen;

        // Load map
        this.map = (new TmxMapLoader()).load(levelDescriptor.mapFileName);
        this.renderer = new OrthoCachedTiledMapRenderer(map);
        ((OrthoCachedTiledMapRenderer) renderer).setBlending(true);

        // Load map properties
        this.name = map.getProperties().get("name", "[UNNAMED]", String.class);

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
        playerSpawn = null;
        enemySpawns = new Array<>();
        exit = null;

        MapObjects objects = objectsLayer.getObjects();
        for (MapObject object : objects) {
            MapProperties props = object.getProperties();
            String type = (String) props.get("type");
            if (type == null) {
                Gdx.app.log("Map", "Map object missing 'type' property");
                continue;
            }

            if ("spawn-player".equalsIgnoreCase(type)) {
                float x = props.get("x", Float.class);
                float y = props.get("y", Float.class);
                playerSpawn = new SpawnPlayer(x, y, assets);
            }
            else if ("spawn-enemy".equalsIgnoreCase(type)) {
                float x = props.get("x", Float.class);
                float y = props.get("y", Float.class);
                SpawnEnemy spawn = new SpawnEnemy(x, y, assets);
                // TODO: figure out what type of enemy and its direction and whatever and spawn it
//            else if ("spawnEnemy".equalsIgnoreCase(type)) {
//                float x = props.get("x", Float.class);
//                float y = props.get("y", Float.class);
//
//                String directionProp = props.get("direction", "left", String.class).toLowerCase();
//                GameEntity.Direction direction = GameEntity.Direction.left;
//                if ("left".equals(directionProp)) direction = GameEntity.Direction.left;
//                else if ("right".equals(directionProp)) direction = GameEntity.Direction.right;
//                else Gdx.app.log("Map", "Unknown direction for spawnEnemy: '" + directionProp + "'");
//
//                String name = object.getName().toLowerCase();
//                EnemySpawner.EnemyType enemyType = null;
//                if      ("chicken" .equals(name)) enemyType = EnemySpawner.EnemyType.chicken;
//                else if ("bunny"   .equals(name)) enemyType = EnemySpawner.EnemyType.bunny;
//                else Gdx.app.log("Map", "Unknown enemy type for spawnEnemy entity: '" + name + "'");
//
//                if (enemyType != null) {
//                    EnemySpawner spawner = new EnemySpawner(x, y, enemyType, direction);
//                    enemySpawners.add(spawner);
//                    spawner.spawnEnemy(screen);
//                }
//            }
                enemySpawns.add(spawn);
            }
            else if ("exit".equalsIgnoreCase(type)) {
                float x = props.get("x", Float.class);
                float y = props.get("y", Float.class);
                exit = new Exit(x, y, assets);
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

    }

    public void update(float dt) {
        // TODO
    }

    public void render(LayerType layerType, OrthographicCamera camera) {
        Layer layer = layers.get(layerType);
        if (layer == null || layer.tileLayer == null || layer.index.length != 1) return;

        renderer.setView(camera);
        renderer.render(layer.index);
    }


    public void renderObjectsDebug(SpriteBatch batch) {
        exit.render(batch);
        enemySpawns.forEach(spawn -> spawn.render(batch));
        playerSpawn.render(batch);
    }

    public void renderDebug(SpriteBatch batch) {
        float width = 3;
        float hue = 0;
        for (Segment2D segment : collisionSegments){
            hue += .17;
            batch.setColor(Utils.hsvToRgb(hue, 1f, 1f, null));
            batch.draw(screen.assets.whitePixel, segment.start.x, segment.start.y - width/2f, 0, width/2f, segment.delta.len(), width, 1, 1, segment.getRotation());
        }
        batch.setColor(Color.WHITE);
    }

    private void buildCollisionBounds() {
        collisionSegments = new Array<>();
        TiledMapTileLayer collisionLayer = layers.get(LayerType.collision).tileLayer;
        float tileWidth = collisionLayer.getTileWidth();
        // Build Edges
        for (int x = 0; x < collisionLayer.getWidth(); x++) {
            for (int y =0; y < collisionLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                TiledMapTileLayer.Cell cellRight = collisionLayer.getCell(x +1, y);
                TiledMapTileLayer.Cell cellLeft = collisionLayer.getCell(x -1, y);
                TiledMapTileLayer.Cell cellTop = collisionLayer.getCell(x, y +1);
                TiledMapTileLayer.Cell cellBottom = collisionLayer.getCell(x, y -1);
                if (cell != null && cellRight == null) {
                    collisionSegments.add(new Segment2D((x+1) * tileWidth, y * tileWidth, (x+1) * tileWidth, (y+1) * tileWidth));
                }
                if (cell != null && cellLeft == null) {
                    collisionSegments.add(new Segment2D((x) * tileWidth, (y+1) * tileWidth, (x) * tileWidth, (y) * tileWidth));
                }
                if (cell != null && cellTop == null) {
                    collisionSegments.add(new Segment2D((x+1) * tileWidth, (y+1) * tileWidth, (x) * tileWidth, (y+1) * tileWidth));
                }
                if (cell != null && cellBottom == null) {
                    collisionSegments.add(new Segment2D((x) * tileWidth, (y) * tileWidth, (x+1) * tileWidth, (y) * tileWidth));
                }
            }
        }

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
        int i = 0;
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