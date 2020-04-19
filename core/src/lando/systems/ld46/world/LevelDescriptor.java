package lando.systems.ld46.world;

public enum LevelDescriptor {

      test("maps/test.tmx")
    , level1("maps/level-1.tmx")
    ;

    public String mapFileName;

    LevelDescriptor(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    @Override
    public String toString() {
        return "[Level: " + mapFileName + "]";
    }

}
