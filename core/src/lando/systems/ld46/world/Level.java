package lando.systems.ld46.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthoCachedTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.*;
import lando.systems.ld46.Assets;
import lando.systems.ld46.screens.GameScreen;

public class Level {

    public static final float TILE_SIZE = 32f;

    public enum Layer { background, collision, foreground }

    private Assets assets;
    private GameScreen screen;

    public String name;
    public TiledMap map;
    public TiledMapRenderer renderer;
    public ObjectMap<Layer, TiledMapTileLayer> layers;

    public MapLayer objectsLayer;
//    public SpawnPlayer playerSpawn;
//    public Array<SpawnEnemy> enemySpawns;
//    public Exit exit;

    public Pool<Rectangle> rectPool = Pools.get(Rectangle.class);
    private Rectangle tempRect = new Rectangle();
    private Array<Rectangle> tileRects = new Array<>();

    public Level(LevelDescriptor levelDescriptor, GameScreen screen) {
        Gdx.app.log("Level", "Loading level: '" + levelDescriptor.toString() + "'");

        this.assets = screen.game.assets;
        this.screen = screen;

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

        // Validate map layers
        MapLayers layers = map.getLayers();
        this.layers = new ObjectMap<>();
        this.layers.put(Layer.background, (TiledMapTileLayer) layers.get("background"));
        this.layers.put(Layer.collision, (TiledMapTileLayer) layers.get("collision"));
        this.layers.put(Layer.foreground, (TiledMapTileLayer) layers.get("foreground"));
        this.objectsLayer = layers.get("objects");
        if (this.layers.get(Layer.background) == null || this.layers.get(Layer.collision) == null || this.layers.get(Layer.foreground) == null || this.objectsLayer == null) {
            throw new GdxRuntimeException("Missing required map layer. (required: 'background', 'collision', 'foreground', 'objects')");
        }

        // Load map objects
//        enemySpawners = new Array<EnemySpawner>();
//        exits = new Array<Exit>();
        MapObjects objects = objectsLayer.getObjects();
        for (MapObject object : objects) {
            MapProperties props = object.getProperties();
            String type = (String) props.get("type");
            if (type == null) {
                Gdx.app.log("Map", "Map object missing 'type' property");
                continue;
            }

            // TODO: instantiate objects

//            if ("spawnPlayer".equalsIgnoreCase(type)) {
//                float x = props.get("x", Float.class);
//                float y = props.get("y", Float.class);
//                playerSpawn = new SpawnPlayer(x, y, assets);
//            }
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
//            else if ("exit".equalsIgnoreCase(type)) {
//                float x = props.get("x", Float.class);
//                float y = props.get("y", Float.class);
//                exits.add(new Exit(x, y, assets));
//            }
        }

        // Validate that we have required entities
//        if (playerSpawn == null) {
//            throw new GdxRuntimeException("Missing required map object: 'spawnPlayer'");
//        }
    }

    public void update(float dt) {
        // TODO
    }

    public void render(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
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

        TiledMapTileLayer collisionLayer = layers.get(Layer.collision);
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
