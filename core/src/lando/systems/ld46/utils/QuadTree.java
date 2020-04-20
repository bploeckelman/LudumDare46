package lando.systems.ld46.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import lando.systems.ld46.Assets;

public class QuadTree {

    public static final int MAX_ENTITIES = 3;
    public static final int MAX_LEVELS = 10;

    private Rectangle bounds;

    private Array<QuadTreeable> entities;

    private int level;

    private Array<QuadTree> childNodes;

    private Assets assets;

    // Temp variable
    private static Vector2 CENTER;

    public QuadTree(Assets assets, int level, Rectangle bounds){
        this.assets = assets;
        this.level = level;
        this.bounds = bounds;

        entities = new Array<QuadTreeable>();
        childNodes = new Array<QuadTree>(true, 4);
        if (CENTER == null){
            CENTER = new Vector2();
        }
    }

    public void clear() {
        entities.clear();
        for (QuadTree currentNode : childNodes){
            if (currentNode != null) {
                currentNode.clear();
            }
        }
        childNodes.clear();
    }

    private void split() {
        float halfWidth = bounds.width/2f;
        float halfHeight = bounds.height/2f;
        float x = bounds.x;
        float y = bounds.y;

        Rectangle nwRect = new Rectangle(x, y+halfHeight, halfWidth, halfHeight);
        childNodes.add(new QuadTree(assets, level +1, nwRect));

        Rectangle neRect = new Rectangle(x + halfWidth, y + halfHeight, halfWidth, halfHeight);
        childNodes.add(new QuadTree(assets, level + 1, neRect));

        Rectangle swRect = new Rectangle(x, y, halfWidth, halfHeight);
        childNodes.add(new QuadTree(assets, level +1, swRect));

        Rectangle seRect = new Rectangle(x + halfWidth, y, halfWidth, halfHeight);
        childNodes.add(new QuadTree(assets, level + 1, seRect));
    }

    private int getIndex(QuadTreeable entity){
        int index = -1;
        CENTER = bounds.getCenter(CENTER);
        // Object fits completely in the top
        Rectangle collisionBounds = entity.getCollisionRect();
        boolean topQuadrant = collisionBounds.y > CENTER.y;

        // Object Fits completely in the bottom
        boolean bottomQuadrant = collisionBounds.y + collisionBounds.height < CENTER.y;

        if (collisionBounds.x + collisionBounds.width < CENTER.x) {
            if (topQuadrant) index = 0;
            else if (bottomQuadrant) index = 2;
        } else if (collisionBounds.x > CENTER.x){
            if (topQuadrant) index = 1;
            else if (bottomQuadrant) index = 3;
        }

        return index;
    }

    public void insert(QuadTreeable entity) {
        if (childNodes.size > 0){
            int index = getIndex(entity);
            if (index != -1){
                childNodes.get(index).insert(entity);
                return;
            }
        }
        entities.add(entity);

        if (entities.size > MAX_ENTITIES && level < MAX_LEVELS && childNodes.size == 0) {
            split();
            int i = 0;
            while ( i < entities.size) {
                int index = getIndex(entities.get(i));
                if (index != -1) {
                    QuadTreeable poppedEntity = entities.removeIndex(i);
                    QuadTree nodeToAdd = childNodes.get(index);
                    nodeToAdd.insert(poppedEntity);
                } else {
                    i++;
                }
            }
        }
    }

    public Array<QuadTreeable> retrieve(Array<QuadTreeable> entitiesToReturn, QuadTreeable entityToSearch) {
        if (childNodes.size > 0) {
            int index = getIndex(entityToSearch);

            if (index != -1) {
                childNodes.get(index).retrieve(entitiesToReturn, entityToSearch);
            } else {
                for(QuadTree node : childNodes){
                    node.retrieve(entitiesToReturn, entityToSearch);
                }
            }
        }

        entitiesToReturn.addAll(entities);

        return entitiesToReturn;
    }

    public void renderDebug(SpriteBatch batch){
        float color = entities.size / 10f;
        batch.setColor(color, 0, 0, 1f);
        if (entities.size > 0) {
            batch.draw(assets.whitePixel, bounds.x, bounds.y, bounds.width, bounds.height);
        }
        for (QuadTree node : childNodes){
            node.renderDebug(batch);
        }
        batch.setColor(Color.WHITE);
    }
}
